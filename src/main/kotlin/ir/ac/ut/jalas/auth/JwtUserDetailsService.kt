package ir.ac.ut.jalas.auth

import ir.ac.ut.jalas.exceptions.EntityNotFoundError
import ir.ac.ut.jalas.repositories.UserRepository
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class JwtUserDetailsService(val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
                ?: throw EntityNotFoundError(ErrorType.USER_NOT_FOUND)
        return User(user.email, user.password, emptyList())
    }
}