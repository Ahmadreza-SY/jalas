package ir.ac.ut.jalas.controllers

import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.controllers.models.*
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.exceptions.BadRequestError
import ir.ac.ut.jalas.services.MeetingService
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/meeting")
class MeetingController(val meetingService: MeetingService) {

    @GetMapping
    fun getMeetings() = meetingService.getMeetings()

    @GetMapping("/polls")
    fun getMyPolls() = meetingService.getMyPolls()

    @PostMapping
    fun createMeeting(@Valid @RequestBody request: MeetingCreationRequest): MeetingResponse {
        if (request.slots.any { it.notValid() })
            throw BadRequestError(ErrorType.INVALID_DATE_RANGE)
        return meetingService.createMeeting(request)
    }

    @GetMapping("/{meetingId}")
    fun getMeeting(@PathVariable meetingId: String) = meetingService.getMeeting(meetingId)

    @PutMapping("/{meetingId}")
    fun updateMeetingSlots(
            @PathVariable meetingId: String,
            @Valid @RequestBody updateRequest: MeetingUpdateRequest
    ) = meetingService.updateMeeting(meetingId, updateRequest)

    @PutMapping("/{meetingId}/vote")
    fun voteForMeeting(
            @PathVariable meetingId: String,
            @Valid @RequestBody request: VoteRequest
    ) {
        if (request.slot.notValid())
            throw BadRequestError(ErrorType.INVALID_DATE_RANGE)
        meetingService.voteForMeeting(meetingId, request)
    }

    @GetMapping("/available-rooms")
    fun getAvailableRooms(
            @RequestParam start: Long,
            @RequestParam end: Long
    ): AvailableRoomsResponse {
        val time = TimeRange(start, end)
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
        if (request.pageEntryTime.time > Date().time)
            throw BadRequestError(ErrorType.INVALID_TIME)
        meetingService.reserveMeeting(meetingId, request)
    }

    @DeleteMapping("/{meetingId}/reserve")
    fun cancelMeetingReservation(@PathVariable meetingId: String) =
            meetingService.cancelMeetingReservation(meetingId)

    @PostMapping("/{meetingId}/comment")
    fun addCommentToMeeting(
            @PathVariable meetingId: String,
            @Valid @RequestBody request: CommentCreationRequest
    ) = meetingService.addCommentToMeeting(meetingId, request)

}
