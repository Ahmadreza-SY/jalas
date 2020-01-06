package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Comment
import java.util.*

data class CommentDto(
        val id: String? = UUID.randomUUID().toString(),
        val owner: String,
        val content: String,
        val creationDate: Long,
        val replies: List<CommentDto>,
        val meetingId: String
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
            id = entity.id,
            owner= entity.owner,
            content = entity.content,
            creationDate = entity.creationDate.time,
            replies = entity.replies.map { CommentDto(it) },
            meetingId = entity.meetingId
    )
}