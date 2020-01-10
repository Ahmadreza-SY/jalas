package ir.ac.ut.jalas.controllers.models.users

import com.fasterxml.jackson.annotation.JsonProperty
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.NotificationType

data class UserResponse(
        val id: String? = null,
        val email: String,
        val firstName: String,
        val lastName: String,
        val notificationTypes: List<NotificationType>,
        @get:JsonProperty("isAdmin")
        val isAdmin: Boolean
) {
    constructor(entity: User, isAdmin: Boolean) : this(
            entity.id,
            entity.email,
            entity.firstName,
            entity.lastName,
            entity.notificationTypes,
            isAdmin
    )
}