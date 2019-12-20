package ir.ac.ut.jalas.services

import feign.FeignException
import ir.ac.ut.jalas.clients.ReservationClient
import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.clients.models.ReservationRequest
import ir.ac.ut.jalas.controllers.models.*
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.exceptions.*
import ir.ac.ut.jalas.repositories.MeetingRepository
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

    private fun notifyGuests(meeting: Meeting, owner: User) {
        meeting.guests.onEach { guest ->
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
                    to = guest
            )
        }
    }

    fun getMeeting(meetingId: String): MeetingResponse {
        val entity = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        return MeetingResponse(entity)
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
            val user = authService.getLoggedInUser()
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
                    to = participant
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

        if (!meeting.guests.contains(request.email.toLowerCase()))
            throw AccessDeniedError(ErrorType.NOT_MEETING_GUEST)

        val slot = meeting.slots.firstOrNull { it.time == request.slot }
                ?: throw EntityNotFoundError(ErrorType.SLOT_NOT_FOUND)

        when (request.vote) {
            VoteOption.AGREE -> {
                if (slot.agreeingUsers.contains(request.email))
                    throw PreconditionFailedError(ErrorType.USER_ALREADY_VOTED)
                slot.agreeingUsers += request.email
                slot.disagreeingUsers -= request.email
            }
            VoteOption.DISAGREE -> {
                if (slot.disagreeingUsers.contains(request.email))
                    throw PreconditionFailedError(ErrorType.USER_ALREADY_VOTED)
                slot.disagreeingUsers += request.email
                slot.agreeingUsers -= request.email
            }
            VoteOption.REVOKE -> {
                when {
                    slot.agreeingUsers.contains(request.email) -> slot.agreeingUsers -= request.email
                    slot.disagreeingUsers.contains(request.email) -> slot.disagreeingUsers -= request.email
                    else -> throw PreconditionFailedError(ErrorType.USER_NOT_VOTED)
                }
            }
        }
        meetingRepository.save(meeting)
    }
}