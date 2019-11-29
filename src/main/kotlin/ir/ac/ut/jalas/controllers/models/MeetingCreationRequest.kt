package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.TimeSlot
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class MeetingCreationRequest(
        @field:NotBlank
        val title: String,
        @field:NotNull
        @field:NotEmpty
        val slots: List<TimeSlot>
) {
    fun extract(owner: String) = Meeting(
            title = title,
            slots = slots,
            owner = owner,
            status = MeetingStatus.ELECTING
    )
}
