package ir.ac.ut.jalas.controllers.models

import com.fasterxml.jackson.annotation.JsonProperty
import ir.ac.ut.jalas.entities.User

data class UserResponse(
        val id: String? = null,
        val email: String,
        val firstName: String,
        val lastName: String,
        @get:JsonProperty("isAdmin")
        val isAdmin: Boolean
) {
    constructor(entity: User, isAdmin: Boolean) : this(entity.id, entity.email, entity.firstName, entity.lastName, isAdmin)
}