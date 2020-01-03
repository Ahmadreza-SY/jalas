package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Comment
import java.util.*

data class CommentResponse(
        val id: String,
        val owner: String,
        val content: String,
        val creationDate: Long,
        val replies: List<CommentResponse>
) {
    constructor(entity: Comment) : this(
            entity.id ?: "NA",
            entity.owner,
            entity.content,
            entity.creationDate.time,
            entity.replies.map { CommentResponse(it) }
    )
}