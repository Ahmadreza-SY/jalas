package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.nested.TimeRange
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class MeetingSlotDeleteRequest(
        @field:NotNull
        val slot: TimeRange
)