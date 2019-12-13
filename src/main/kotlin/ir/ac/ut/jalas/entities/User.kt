package ir.ac.ut.jalas.entities

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
        val password: String
) {
    fun fullName(): String = "$firstName $lastName"
}