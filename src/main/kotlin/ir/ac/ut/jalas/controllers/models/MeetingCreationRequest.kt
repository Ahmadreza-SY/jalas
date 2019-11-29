package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingPoll
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class MeetingCreationRequest(
        @field:NotBlank
        val title: String,
        @field:NotNull
        @field:NotEmpty
        val votes: List<MeetingPoll>
) {
    fun extract(owner: String) = Meeting(
            title = title,
            votes = votes,
            owner = owner,
            status = MeetingStatus.ELECTING
    )
}
