package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.nested.MeetingTime
import javax.validation.constraints.NotNull

data class MeetingReservationRequest(
        @field:NotNull
        val selectedTime: MeetingTime,
        @field:NotNull
        val selectedRoom: Int
)