package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.entities.nested.TimeSlot
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class MeetingCreationRequest(
        @field:NotBlank
        val title: String,
        @field:NotNull
        @field:NotEmpty
        val slots: List<TimeRange>,
        @field:NotNull
        @field:NotEmpty
        val guests: List<String>
) {
    fun extract(owner: String) = Meeting(
            title = title,
            slots = slots.map { TimeSlot(agreeingUsers = mutableListOf(), disagreeingUsers = mutableListOf(), time = it) },
            owner = owner,
            status = MeetingStatus.ELECTING,
            guests = guests.map { it.toLowerCase() }
    )
}
