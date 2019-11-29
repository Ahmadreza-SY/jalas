package ir.ac.ut.jalas.controllers

import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.controllers.models.MeetingReservationRequest
import ir.ac.ut.jalas.entities.nested.MeetingTime
import ir.ac.ut.jalas.exceptions.BadRequestError
import ir.ac.ut.jalas.services.MeetingService
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping("/meeting")
class MeetingController(val meetingService: MeetingService) {

    @GetMapping
    fun getMeetings() = meetingService.getMeetings()

    @GetMapping("/{meetingId}")
    fun getMeeting(@PathVariable meetingId: String) = meetingService.getMeeting(meetingId)

    @GetMapping("/available-rooms")
    fun getAvailableRooms(
            @RequestParam start: Long,
            @RequestParam end: Long
    ): AvailableRoomsResponse {
        val time = MeetingTime(start, end)
        if (time.notValid())
            throw BadRequestError(ErrorType.INVALID_DATE_RANGE)
        return meetingService.getAvailableRooms(time)
    }

    @PostMapping("/{meetingId}/reserve")
    fun reserveMeeting(
            @PathVariable meetingId: String,
            @Valid @RequestBody request: MeetingReservationRequest
    ) {
        if (request.selectedTime.notValid())
            throw BadRequestError(ErrorType.INVALID_DATE_RANGE)
        meetingService.reserveMeeting(meetingId, request)
    }

}
