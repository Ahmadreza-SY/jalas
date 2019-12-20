package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.User

data class UserResponse(
        val id: String? = null,
        val email: String,
        val firstName: String,
        val lastName: String
) {
    constructor(entity: User) : this(entity.id, entity.email, entity.firstName, entity.lastName)
}