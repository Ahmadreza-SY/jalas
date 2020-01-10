package ir.ac.ut.jalas.entities

import ir.ac.ut.jalas.entities.nested.NotificationType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user")
data class User(
        @Id
        val id: String? = null,
        @Indexed(unique = true)
        val email: String,
        val firstName: String,
        val lastName: String,
        val password: String,
        var notificationTypes: List<NotificationType>
) {
    fun fullName(): String = "$firstName $lastName"
}