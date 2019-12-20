package ir.ac.ut.jalas.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "comment")
data class Comment(
        @Id
        var id: String? = null,
        val owner: String,
        val meetingId: String,
        val content: String,
        val creationDate: Date
)