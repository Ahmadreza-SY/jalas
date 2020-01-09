package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.entities.nested.TimeSlot
import java.util.*
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
        val guests: List<String>,
        val deadline: Date?
) {
    fun extract(owner: String) = Meeting(
            title = title,
            slots = slots.map {
                TimeSlot(
                        agreeingUsers = mutableListOf(),
                        disagreeingUsers = mutableListOf(),
                        agreeIfNeededUsers = mutableListOf(),
                        time = it
                )
            }.toMutableList(),
            owner = owner,
            status = MeetingStatus.ELECTING,
            guests = guests.map { it.toLowerCase() }.toMutableList(),
            deadline = deadline
    )
}
