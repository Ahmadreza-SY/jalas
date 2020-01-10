package ir.ac.ut.jalas.controllers.models.comment

import ir.ac.ut.jalas.entities.Comment
import java.util.*
import javax.validation.constraints.NotBlank

data class CommentCreationRequest(
        @field:NotBlank
        val content: String
) {
    fun extract(meetingId: String, owner: String) = Comment(
            meetingId = meetingId,
            owner = owner,
            content = content,
            creationDate = Date(),
            replies = emptyList()
    )
}
