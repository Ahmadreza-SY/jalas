package ir.ac.ut.jalas.controllers.models.meetings

import ir.ac.ut.jalas.entities.nested.TimeRange
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class MeetingUpdateRequest(
        @field:NotNull
        @field:NotEmpty
        val newSlots: List<TimeRange>
)