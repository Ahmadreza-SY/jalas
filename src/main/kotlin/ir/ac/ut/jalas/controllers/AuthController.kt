package ir.ac.ut.jalas.controllers

import ir.ac.ut.jalas.auth.JwtTokenUtil
import ir.ac.ut.jalas.auth.JwtUserDetailsService
import ir.ac.ut.jalas.controllers.models.users.JwtRequest
import ir.ac.ut.jalas.controllers.models.users.JwtResponse
import ir.ac.ut.jalas.controllers.models.users.NotificationUpdateRequest
import ir.ac.ut.jalas.controllers.models.users.UserResponse
import ir.ac.ut.jalas.services.AuthService
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
@CrossOrigin
class AuthController(
        val authenticationManager: AuthenticationManager,
        val jwtTokenUtil: JwtTokenUtil,
        val userDetailsService: JwtUserDetailsService,
        val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody authenticationRequest: JwtRequest): ResponseEntity<*> {
        authenticate(authenticationRequest.username, authenticationRequest.password)
        val userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
        val token = jwtTokenUtil.generateToken(userDetails)
        return ResponseEntity.ok(JwtResponse(token))
    }

    @GetMapping("/profile")
    fun getProfile() = UserResponse(authService.getLoggedInUser(), authService.isAdmin())

    @PatchMapping("/profile/notification")
    fun updateNotificationTypes(@Valid @RequestBody request: NotificationUpdateRequest) =
            authService.updateNotificationTypes(request)

    private fun authenticate(username: String, password: String) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        } catch (e: DisabledException) {
            throw Exception(ErrorType.USER_DISABLED.name, e)
        } catch (e: BadCredentialsException) {
            throw Exception(ErrorType.INVALID_CREDENTIALS.name, e)
        }
    }
}