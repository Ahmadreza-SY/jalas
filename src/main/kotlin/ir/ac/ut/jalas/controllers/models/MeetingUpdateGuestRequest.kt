package ir.ac.ut.jalas.controllers.models

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class MeetingUpdateGuestRequest(
        @field:NotNull
        @field:NotBlank
        val guest: String
)