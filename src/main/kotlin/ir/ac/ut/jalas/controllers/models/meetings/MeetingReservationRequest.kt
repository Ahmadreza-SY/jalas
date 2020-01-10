package ir.ac.ut.jalas.controllers.models.meetings

import ir.ac.ut.jalas.entities.nested.TimeRange
import java.util.*
import javax.validation.constraints.NotNull

data class MeetingReservationRequest(
        @field:NotNull
        val selectedTime: TimeRange,
        @field:NotNull
        val selectedRoom: Int,
        @field:NotNull
        val pageEntryTime: Date
)