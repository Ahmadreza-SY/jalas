package ir.ac.ut.jalas.services

import feign.FeignException
import ir.ac.ut.jalas.clients.ReservationClient
import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.clients.models.ReservationRequest
import ir.ac.ut.jalas.controllers.models.MeetingSlotDeleteRequest
import ir.ac.ut.jalas.controllers.models.MeetingSlotsUpdateRequest
import ir.ac.ut.jalas.controllers.models.comment.CommentCreationRequest
import ir.ac.ut.jalas.controllers.models.comment.CommentDto
import ir.ac.ut.jalas.controllers.models.meetings.*
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.exceptions.AccessDeniedError
import ir.ac.ut.jalas.exceptions.BadRequestError
import ir.ac.ut.jalas.exceptions.EntityNotFoundError
import ir.ac.ut.jalas.exceptions.InternalServerError
import ir.ac.ut.jalas.repositories.MeetingRepository
import ir.ac.ut.jalas.repositories.UserRepository
import ir.ac.ut.jalas.utils.ErrorType
import ir.ac.ut.jalas.utils.extractErrorMessage
import ir.ac.ut.jalas.utils.toReserveFormat
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class MeetingService(
        val meetingRepository: MeetingRepository,
        val reservationClient: ReservationClient,
        val authService: AuthService,
        val commentService: CommentService,
        val userRepository: UserRepository,
        val notificationService: NotificationService
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
        notificationService.notifyGuests(entity, owner)
        return MeetingResponse(entity)
    }

    fun updateMeetingSlots(meetingId: String, slotsUpdateRequest: MeetingSlotsUpdateRequest): MeetingResponse {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)
        meeting.updateSlots(slotsUpdateRequest)
        meetingRepository.save(meeting)
        val comments = commentService.getComments(meetingId)
        return MeetingResponse(meeting, comments)
    }

    fun deleteMeetingSlot(meetingId: String, deleteRequest: MeetingSlotDeleteRequest): MeetingResponse {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)
        meeting.deleteSlot(deleteRequest)
        meetingRepository.save(meeting)
        val comments = commentService.getComments(meetingId)
        return MeetingResponse(meeting, comments)
    }

    fun addMeetingGuest(meetingId: String, request: MeetingUpdateGuestRequest) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)
        val owner = userRepository.findByEmail(meeting.owner)
                ?: throw BadRequestError(ErrorType.USER_NOT_FOUND)
        meeting.addGuest(request)
        meetingRepository.save(meeting)
        notificationService.notifyGuest(meeting, owner, request.guest)
    }

    fun deleteMeetingGuest(meetingId: String, request: MeetingUpdateGuestRequest) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw BadRequestError(ErrorType.MEETING_NOT_FOUND)
        val owner = userRepository.findByEmail(meeting.owner)
                ?: throw BadRequestError(ErrorType.USER_NOT_FOUND)
        meeting.deleteGuest(request)
        meetingRepository.save(meeting)
        notificationService.notifyGuestRemoval(meeting, owner, request.guest)
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
            meeting.reserve(selectedTime, selectedRoom)
            notificationService.notifySuccessReservation(user, meeting)
            response.message
        } catch (e: FeignException) {
            if (e.status() == HttpStatus.BAD_REQUEST.value()) {
                meeting.toElecting()
            } else {
                meeting.toPending(selectedTime, selectedRoom)
            }
            e.extractErrorMessage()
        }
        meetingRepository.save(meeting)
        return message
    }

    fun cancelMeetingReservation(meetingId: String) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        meeting.cancel()
        meetingRepository.save(meeting)
    }

    fun voteForMeeting(meetingId: String, request: VoteRequest) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        meeting.addVote(request)
        meetingRepository.save(meeting)
        notificationService.sendNewVoteNotification(request, meeting)
    }

    fun addCommentToMeeting(meetingId: String, request: CommentCreationRequest): CommentDto {
        val user = authService.getLoggedInUser()
        checkCommentAuthorization(meetingId, user)
        return commentService.createComment(meetingId, user.email, request)
    }

    fun getMyPolls(): List<Meeting> {
        return meetingRepository.findByOwnerAndStatus(authService.getLoggedInUser().email, MeetingStatus.ELECTING)
    }

    fun checkCommentAuthorization(meetingId: String, user: User) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        if (!meeting.isParticipant(user.email))
            throw AccessDeniedError(ErrorType.NOT_MEETING_GUEST)
    }

    fun closeMeetingPoll(meetingId: String) {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        meeting.closePoll(authService.getLoggedInUser())
        meetingRepository.save(meeting)
    }
}