package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.exceptions.EntityNotFoundError
import ir.ac.ut.jalas.repositories.UserRepository
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service

@Service
class AuthService(val userRepository: UserRepository) {

    companion object {
        val ADMIN_EMAILS = setOf("mohammad.76kiani@gmail.com")
    }

    private fun getLoggedInUsername() =
            (SecurityContextHolder.getContext().authentication.principal as User).username

    fun getLoggedInUser() = userRepository.findByEmail(getLoggedInUsername())
            ?: throw EntityNotFoundError(ErrorType.USER_NOT_FOUND)

    fun isAdmin(): Boolean = getLoggedInUsername() in ADMIN_EMAILS
}