package ir.ac.ut.jalas.services

import feign.FeignException
import ir.ac.ut.jalas.clients.ReservationClient
import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.clients.models.ReservationRequest
import ir.ac.ut.jalas.controllers.models.MeetingReservationRequest
import ir.ac.ut.jalas.controllers.models.MeetingResponse
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.MeetingTime
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

@Service
class MeetingService(
        val meetingRepository: MeetingRepository,
        val reservationClient: ReservationClient
) {

    fun getMeetings() = meetingRepository.findAll().map { MeetingResponse(it) }

    fun getAvailableRooms(time: MeetingTime): AvailableRoomsResponse {
        try {
            return reservationClient.getAvailableRooms(
                    start = time.start.toReserveFormat(),
                    end = time.end.toReserveFormat()
            )
        } catch (e: FeignException) {
            throw InternalServerError(e.extractErrorMessage())
        }
    }

    fun getMeeting(meetingId: String): MeetingResponse {
        val entity = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        return MeetingResponse(entity)
    }

    fun reserveMeeting(meetingId: String, request: MeetingReservationRequest): String? {
        val meeting = meetingRepository.findByIdOrNull(meetingId)
                ?: throw EntityNotFoundError(ErrorType.MEETING_NOT_FOUND)
        val message = reserveMeeting(meeting, request.selectedRoom, request.selectedTime)
        return when (meeting.status) {
            MeetingStatus.RESERVED -> message
            MeetingStatus.PENDING -> throw InternalServerError(message)
            MeetingStatus.ELECTING -> throw BadRequestError(message)
            else -> message
        }
    }

    fun reserveMeeting(meeting: Meeting, selectedRoom: Int, selectedTime: MeetingTime): String? {
        val message = try {
            val response = reservationClient.reserveRoom(
                    roomId = selectedRoom,
                    request = ReservationRequest(
                            username = "ahmadreza",
                            start = selectedTime.start.toReserveFormat(),
                            end = selectedTime.end.toReserveFormat()
                    )
            )

            meeting.status = MeetingStatus.RESERVED
            meeting.time = selectedTime
            meeting.roomId = selectedRoom

            response.message
        } catch (e: FeignException) {
            if (e.status() == HttpStatus.BAD_REQUEST.value()) {
                meeting.status = MeetingStatus.ELECTING
                meeting.time = null
                meeting.roomId = null

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
}