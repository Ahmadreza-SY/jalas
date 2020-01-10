package ir.ac.ut.jalas.services

import feign.FeignException
import ir.ac.ut.jalas.clients.ReservationClient
import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.clients.models.ReservationRequest
import ir.ac.ut.jalas.controllers.models.comment.CommentCreationRequest
import ir.ac.ut.jalas.controllers.models.comment.CommentDto
import ir.ac.ut.jalas.controllers.models.meetings.*
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.NotificationType
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.entities.nested.TimeSlot
import ir.ac.ut.jalas.exceptions.*
import ir.ac.ut.jalas.repositories.MeetingRepository
import ir.ac.ut.jalas.repositories.UserRepository
import ir.ac.ut.jalas.utils.ErrorType
import ir.ac.ut.jalas.utils.extractErrorMessage
import ir.ac.ut.jalas.utils.toReserveFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class MeetingService(
        val meetingRepository: MeetingRepository,
        val reservationClient: ReservationClient,
        val authService: AuthService,
        val mailService: MailService,
        val commentService: CommentService,
        val userRepository: UserRepository,
        @Value("\${jalas.dashboard.url}") val dashboardUrl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    fun getMeetings(): List<MeetingResponse> {
        val user = authService.getLoggedInUser()
        return meetingRepository.findByOwnerOrGuestsIn(user.email, listOf(user.email))
                .map { MeetingResponse(it) }
    }

    fun createMeeting(request: MeetingCreationRequest): MeetingResponse {
        val owner = authService.getLoggedInUser()
        val entity = request.extract(owner.email)
        meetingRepository.save(entity)
        notifyGuests(entity, owner)
        return MeetingResponse(entity)
    }

    fun updateMeeting(meetingId: String, updateRequest: MeetingUpdateRequest): MeetingResponse {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)

        val newSlots = updateRequest.newSlots
        val oldSlots = meeting.slots.map { it.time }
        if (newSlots.any { newSlot -> oldSlots.any { oldSlot -> oldSlot == newSlot } })
            throw BadRequestError(ErrorType.SLOT_ALREADY_EXISTS)

        meeting.slots += updateRequest.newSlots.map { TimeSlot(mutableListOf(), mutableListOf(), mutableListOf(), it) }
        meetingRepository.save(meeting)

        val comments = commentService.getComments(meetingId)
        return MeetingResponse(meeting, comments)
    }

    fun addMeetingGuest(meetingId: String, request: MeetingUpdateGuestRequest) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)

        if (meeting.isParticipant(request.guest))
            throw PreconditionFailedError(ErrorType.USER_ALREADY_INVITED)

        val owner = userRepository.findByEmail(meeting.owner)
                ?: throw BadRequestError(ErrorType.USER_NOT_FOUND)

        meeting.guests += request.guest
        meetingRepository.save(meeting)

        notifyGuest(meeting, owner, request.guest)
    }

    fun deleteMeetingGuest(meetingId: String, request: MeetingUpdateGuestRequest) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)

        if (!meeting.isParticipant(request.guest))
            throw PreconditionFailedError(ErrorType.NOT_MEETING_GUEST)

        val owner = userRepository.findByEmail(meeting.owner)
                ?: throw BadRequestError(ErrorType.USER_NOT_FOUND)

        meeting.guests -= request.guest
        meetingRepository.save(meeting)

        notifyGuestRemoval(meeting, owner, request.guest)
    }

    private fun notifyGuests(meeting: Meeting, owner: User) {
        meeting.guests.onEach { guest ->
            notifyGuest(meeting, owner, guest)
        }
    }

    private fun notifyGuest(meeting: Meeting, owner: User, guest: String) {
        mailService.sendMail(
                subject = "Meeting ${meeting.title} Invitation",
                message = """
                            |Dear Guest,
                            |
                            |You have invited to '${meeting.title}' meeting created by ${owner.fullName()}.
                            |Please visit the following link to vote your available time:
                            |$dashboardUrl/meeting/${meeting.id}/vote/$guest
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                to = guest,
                type = NotificationType.MEETING_INVITATION
        )
    }

    private fun notifyGuestRemoval(meeting: Meeting, owner: User, guest: String) {
        mailService.sendMail(
                subject = "Meeting ${meeting.title} Removal",
                message = """
                            |Dear Guest,
                            |
                            |You have removed from '${meeting.title}' meeting created by ${owner.fullName()}.
                            |We hope seeing you in other meetings
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                to = guest,
                type = NotificationType.MEETING_REMOVE_GUEST
        )
    }

    fun getMeeting(meetingId: String): MeetingResponse {
        val entity = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        val comments = commentService.getComments(meetingId)
        return MeetingResponse(entity, comments)
    }

    fun getAvailableRooms(time: TimeRange): AvailableRoomsResponse {
        try {
            return reservationClient.getAvailableRooms(
                    start = time.start.toReserveFormat(),
                    end = time.end.toReserveFormat()
            )
        } catch (e: FeignException) {
            logger.error("Error in getting available rooms", e)
            throw InternalServerError(e.extractErrorMessage())
        }
    }

    fun reserveMeeting(meetingId: String, request: MeetingReservationRequest): String? {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        meeting.reservationTime = TimeRange(request.pageEntryTime, Date())
        val message = reserveMeeting(meeting, request.selectedRoom, request.selectedTime)
        return when (meeting.status) {
            MeetingStatus.RESERVED -> message
            MeetingStatus.PENDING -> throw InternalServerError(message)
            MeetingStatus.ELECTING -> throw BadRequestError(message)
            else -> message
        }
    }

    fun reserveMeeting(meeting: Meeting, selectedRoom: Int, selectedTime: TimeRange): String? {
        val message = try {
            val user = userRepository.findByEmail(meeting.owner)
                    ?: throw EntityNotFoundError(ErrorType.USER_NOT_FOUND)

            val response = reservationClient.reserveRoom(
                    roomId = selectedRoom,
                    request = ReservationRequest(
                            username = user.email,
                            start = selectedTime.start.toReserveFormat(),
                            end = selectedTime.end.toReserveFormat()
                    )
            )

            meeting.status = MeetingStatus.RESERVED
            meeting.time = selectedTime
            meeting.roomId = selectedRoom
            meeting.reservationTime = meeting.reservationTime?.copy(end = Date())

            notifySuccessReservation(user, meeting)

            response.message
        } catch (e: FeignException) {
            if (e.status() == HttpStatus.BAD_REQUEST.value()) {
                meeting.status = MeetingStatus.ELECTING
                meeting.time = null
                meeting.roomId = null
                meeting.reservationTime = null
            } else {
                meeting.status = MeetingStatus.PENDING
                meeting.time = selectedTime
                meeting.roomId = selectedRoom
            }

            e.extractErrorMessage()
        }

        meetingRepository.save(meeting)

        return message
    }

    private fun notifySuccessReservation(user: User, meeting: Meeting) {
        (meeting.guests + meeting.owner).forEach { participant ->
            val participantName = if (participant == meeting.owner) user.firstName else "Participant"
            mailService.sendMail(
                    subject = "Meeting Reservation Success",
                    message = """
                            |Dear $participantName,
                            |
                            |Your meeting '${meeting.title}' at time [${meeting.time?.start}, ${meeting.time?.end}] has been successfully reserved at room ${meeting.roomId}.
                            |To view more info about the meeting, click on link bellow:
                            |$dashboardUrl/meeting/${meeting.id}
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                    to = participant,
                    type = NotificationType.MEETING_RESERVATION
            )
        }
    }

    fun cancelMeetingReservation(meetingId: String) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        meeting.status = MeetingStatus.CANCELED
        meetingRepository.save(meeting)
    }

    fun voteForMeeting(meetingId: String, request: VoteRequest) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)

        if (meeting.status != MeetingStatus.ELECTING)
            throw PreconditionFailedError(ErrorType.INVALID_MEETING_STATUS)

        if (!meeting.isParticipant(request.email))
            throw AccessDeniedError(ErrorType.NOT_MEETING_GUEST)

        val slot = meeting.slots.firstOrNull { it.time == request.slot }
                ?: throw EntityNotFoundError(ErrorType.SLOT_NOT_FOUND)

        when (request.vote) {
            VoteOption.AGREE -> {
                if (slot.agreeingUsers.contains(request.email))
                    throw PreconditionFailedError(ErrorType.USER_ALREADY_VOTED)
                slot.agreeingUsers += request.email
                slot.disagreeingUsers -= request.email
                slot.agreeIfNeededUsers -= request.email
            }
            VoteOption.DISAGREE -> {
                if (slot.disagreeingUsers.contains(request.email))
                    throw PreconditionFailedError(ErrorType.USER_ALREADY_VOTED)
                slot.disagreeingUsers += request.email
                slot.agreeingUsers -= request.email
                slot.agreeIfNeededUsers -= request.email
            }
            VoteOption.AGREE_IF_NEEDED -> {
                if (slot.agreeIfNeededUsers.contains(request.email))
                    throw PreconditionFailedError(ErrorType.USER_ALREADY_VOTED)
                slot.agreeIfNeededUsers += request.email
                slot.agreeingUsers -= request.email
                slot.disagreeingUsers -= request.email
            }
            VoteOption.REVOKE -> {
                when {
                    slot.agreeingUsers.contains(request.email) -> slot.agreeingUsers -= request.email
                    slot.disagreeingUsers.contains(request.email) -> slot.disagreeingUsers -= request.email
                    slot.agreeIfNeededUsers.contains(request.email) -> slot.agreeIfNeededUsers -= request.email
                    else -> throw PreconditionFailedError(ErrorType.USER_NOT_VOTED)
                }
            }
        }
        sendNewVoteNotification(request, meeting)
        meetingRepository.save(meeting)
    }

    private fun sendNewVoteNotification(request: VoteRequest, meeting: Meeting) {
        mailService.sendMail(
                subject = "New Vote for Meeting: ${meeting.title}",
                message = """
                            |Dear user,
                            |
                            |User with email: ${request.email} has voted for ${request.vote.name} for time slot ${request.slot.start.toReserveFormat()}-${request.slot.end.toReserveFormat()}
                            |To view more info about the meeting, click on link bellow:
                            |$dashboardUrl/meeting/${meeting.id}
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                to = meeting.owner,
                type = NotificationType.MEETING_VOTE
        )
    }

    fun addCommentToMeeting(meetingId: String, request: CommentCreationRequest): CommentDto {
        val user = authService.getLoggedInUser()
        checkCommentAuthorization(meetingId, user)
        return commentService.createComment(meetingId, user.email, request)
    }

    fun getMyPolls(): List<Meeting> {
        return meetingRepository.findByOwnerAndStatus(authService.getLoggedInUser().email, MeetingStatus.ELECTING)
    }

    fun updateComment(meetingId: String, commentDto: CommentDto): CommentDto {
        checkCommentAuthorization(meetingId, authService.getLoggedInUser())
        return commentService.updateComment(meetingId, commentDto)
    }

    private fun checkCommentAuthorization(meetingId: String, user: User) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        if (!meeting.isParticipant(user.email))
            throw AccessDeniedError(ErrorType.NOT_MEETING_GUEST)
    }

    fun deleteComment(commentId: String) {
        commentService.deleteComment(commentId)
    }

    fun closeMeetingPoll(meetingId: String) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)

        val user = authService.getLoggedInUser()
        if (meeting.owner != user.email)
            throw AccessDeniedError(ErrorType.NOT_MEETING_OWNER)

        meeting.status = MeetingStatus.CLOSED
        meetingRepository.save(meeting)
    }
}