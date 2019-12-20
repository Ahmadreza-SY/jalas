package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.entities.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthService {

    companion object {
        val ADMIN_EMAILS = hashSetOf("mohammad.76kiani@gmail.com")
    }

    fun getLoggedInUser() = SecurityContextHolder.getContext().authentication.principal as User

    fun isAdmin(): Boolean = getLoggedInUser().email in ADMIN_EMAILS
}