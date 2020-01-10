package ir.ac.ut.jalas.controllers.models.meetings

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class MeetingUpdateGuestRequest(
        @field:NotNull
        @field:NotBlank
        @field:Email
        val guest: String
)