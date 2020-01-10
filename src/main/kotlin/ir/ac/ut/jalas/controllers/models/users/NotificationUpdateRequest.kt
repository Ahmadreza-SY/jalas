package ir.ac.ut.jalas.controllers.models.users

import ir.ac.ut.jalas.entities.nested.NotificationType
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class NotificationUpdateRequest(
        @field:NotNull
        @field:NotEmpty
        val types: List<NotificationType>
)