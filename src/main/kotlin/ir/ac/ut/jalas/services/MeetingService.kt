package ir.ac.ut.jalas.services

import feign.FeignException
import ir.ac.ut.jalas.clients.ReservationClient
import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.clients.models.ReservationRequest
import ir.ac.ut.jalas.controllers.models.MeetingCreationRequest
import ir.ac.ut.jalas.controllers.models.MeetingReservationRequest
import ir.ac.ut.jalas.controllers.models.MeetingResponse
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.exceptions.BadRequestError
import ir.ac.ut.jalas.exceptions.EntityNotFoundError
import ir.ac.ut.jalas.exceptions.InternalServerError
import ir.ac.ut.jalas.repositories.MeetingRepository
import ir.ac.ut.jalas.utils.ErrorType
import ir.ac.ut.jalas.utils.extractErrorMessage
import ir.ac.ut.jalas.utils.toReserveFormat
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class MeetingService(
        val meetingRepository: MeetingRepository,
        val reservationClient: ReservationClient,
        val authService: AuthService,
        val mailService: MailService
) {

    fun getMeetings() = meetingRepository.findAll().map { MeetingResponse(it) }

    fun createRequest(request: MeetingCreationRequest): MeetingResponse {
        val owner = authService.getLoggedInUser()
        val entity = request.extract(owner.email)
        meetingRepository.save(entity)
        return MeetingResponse(entity)
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
        mailService.sendMail(
                subject = "Meeting Reservation Success",
                message = """
                            |Dear ${user.firstName},
                            |
                            |Your meeting '${meeting.title}' at time [${meeting.time?.start}, ${meeting.time?.end}] has 
                            been successfully reserved at room ${meeting.roomId}.
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                to = meeting.owner
        )
    }
}