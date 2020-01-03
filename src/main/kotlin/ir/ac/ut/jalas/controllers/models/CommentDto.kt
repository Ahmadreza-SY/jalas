package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Comment
import java.util.*

data class CommentDto(
        val id: String,
        val owner: String,
        val content: String,
        val creationDate: Long,
        val replies: List<CommentDto>
) {
    fun extract(meetingId: String): Comment {
        return Comment(
                id = id,
                owner = owner,
                meetingId = meetingId,
                creationDate = Date(creationDate),
                content = content,
                replies = replies.map { it.extract(meetingId) }
        )
    }

    constructor(entity: Comment) : this(
            entity.id ?: "NA",
            entity.owner,
            entity.content,
            entity.creationDate.time,
            entity.replies.map { CommentDto(it) }
    )
}