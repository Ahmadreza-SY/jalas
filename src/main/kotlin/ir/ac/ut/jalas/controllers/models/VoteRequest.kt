package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.nested.TimeRange
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class VoteRequest(
        @field:Email
        @field:NotBlank
        val email: String,

        @field:NotNull
        val slot: TimeRange,

        @field:NotNull
        val vote: VoteOption
)