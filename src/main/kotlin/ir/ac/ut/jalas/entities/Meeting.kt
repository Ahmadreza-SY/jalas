package ir.ac.ut.jalas.entities

import ir.ac.ut.jalas.controllers.models.MeetingSlotDeleteRequest
import ir.ac.ut.jalas.controllers.models.MeetingSlotsUpdateRequest
import ir.ac.ut.jalas.controllers.models.meetings.MeetingUpdateGuestRequest
import ir.ac.ut.jalas.controllers.models.meetings.VoteOption
import ir.ac.ut.jalas.controllers.models.meetings.VoteRequest
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.entities.nested.TimeSlot
import ir.ac.ut.jalas.exceptions.AccessDeniedError
import ir.ac.ut.jalas.exceptions.BadRequestError
import ir.ac.ut.jalas.exceptions.EntityNotFoundError
import ir.ac.ut.jalas.exceptions.PreconditionFailedError
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "meeting")
data class Meeting(
        @Id
        var id: String? = null,
        val title: String,
        var status: MeetingStatus,
        var time: TimeRange? = null,
        val slots: MutableList<TimeSlot> = mutableListOf(),
        var roomId: Int? = null,
        val owner: String,
        var reservationTime: TimeRange? = null,
        val guests: MutableList<String> = mutableListOf(),
        val deadline: Date? = null
) {
    fun isParticipant(userEmail: String): Boolean =
            guests.contains(userEmail.toLowerCase()) || owner == userEmail.toLowerCase()

    fun updateSlots(updateRequest: MeetingSlotsUpdateRequest) {
        val newSlots = updateRequest.newSlots
        val oldSlots = slots.map { it.time }
        if (newSlots.any { newSlot -> oldSlots.any { oldSlot -> oldSlot == newSlot } })
            throw BadRequestError(ErrorType.SLOT_ALREADY_EXISTS)
        slots += updateRequest
                .newSlots
                .map { TimeSlot(mutableListOf(), mutableListOf(), mutableListOf(), it) }

    }

    fun deleteSlot(deleteRequest: MeetingSlotDeleteRequest) {
        val foundSlot = slots.find { it.time == deleteRequest.slot }
                ?: throw BadRequestError(ErrorType.SLOT_NOT_FOUND)
        slots.removeIf { it.time == foundSlot.time }
    }

    fun addGuest(request: MeetingUpdateGuestRequest) {
        if (isParticipant(request.guest))
            throw PreconditionFailedError(ErrorType.USER_ALREADY_INVITED)
        guests += request.guest
    }

    fun deleteGuest(request: MeetingUpdateGuestRequest) {
        if (!isParticipant(request.guest))
            throw PreconditionFailedError(ErrorType.NOT_MEETING_GUEST)
        guests -= request.guest
    }

    fun cancel() {
        status = MeetingStatus.CANCELED
    }

    fun addVote(request: VoteRequest) {
        if (status != MeetingStatus.ELECTING)
            throw PreconditionFailedError(ErrorType.INVALID_MEETING_STATUS)

        if (!isParticipant(request.email))
            throw AccessDeniedError(ErrorType.NOT_MEETING_GUEST)

        val slot = slots.firstOrNull { it.time == request.slot }
                ?: throw EntityNotFoundError(ErrorType.SLOT_NOT_FOUND)

        slot.addVote(request.vote, request.email)
    }

    fun closePoll(user: User) {
        if (owner != user.email)
            throw AccessDeniedError(ErrorType.NOT_MEETING_OWNER)
        status = MeetingStatus.CLOSED
    }

    fun reserve(selectedTime: TimeRange, selectedRoom: Int) {
        status = MeetingStatus.RESERVED
        time = selectedTime
        roomId = selectedRoom
        reservationTime = reservationTime?.copy(end = Date())
    }

    fun toElecting() {
        status = MeetingStatus.ELECTING
        time = null
        roomId = null
        reservationTime = null
    }

    fun toPending(selectedTime: TimeRange, selectedRoom: Int) {
        status = MeetingStatus.PENDING
        time = selectedTime
        roomId = selectedRoom
    }
}