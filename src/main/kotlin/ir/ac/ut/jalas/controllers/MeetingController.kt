package ir.ac.ut.jalas.controllers

import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.entities.nested.MeetingTime
import ir.ac.ut.jalas.exceptions.BadRequestError
import ir.ac.ut.jalas.services.MeetingService
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/meeting")
class MeetingController(val meetingService: MeetingService) {

    @GetMapping("/{meetingId}")
    fun getMeeting(@PathVariable meetingId: String) = meetingService.getMeeting(meetingId)

    @GetMapping("/available-rooms")
    fun getAvailableRooms(
            @RequestParam start: Long,
            @RequestParam end: Long
    ): AvailableRoomsResponse {
        if (start > end)
            throw BadRequestError(ErrorType.INVALID_DATE_RANGE)
        return meetingService.getAvailableRooms(MeetingTime(start, end))
    }

}
