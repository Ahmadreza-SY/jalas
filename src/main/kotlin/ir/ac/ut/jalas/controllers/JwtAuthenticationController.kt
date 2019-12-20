package ir.ac.ut.jalas.controllers

import ir.ac.ut.jalas.auth.JwtTokenUtil
import ir.ac.ut.jalas.auth.JwtUserDetailsService
import ir.ac.ut.jalas.controllers.models.JwtRequest
import ir.ac.ut.jalas.controllers.models.JwtResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class JwtAuthenticationController(
        val authenticationManager: AuthenticationManager,
        val jwtTokenUtil: JwtTokenUtil,
        val userDetailsService: JwtUserDetailsService
) {
    @PostMapping("/authenticate")
    fun createAuthenticationToken(@RequestBody authenticationRequest: JwtRequest): ResponseEntity<*> {
        authenticate(authenticationRequest.username, authenticationRequest.password)
        val userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.username)
        val token = jwtTokenUtil.generateToken(userDetails)
        return ResponseEntity.ok(JwtResponse(token))
    }

    private fun authenticate(username: String, password: String) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        } catch (e: DisabledException) {
            throw Exception("USER_DISABLED", e)
        } catch (e: BadCredentialsException) {
            throw Exception("INVALID_CREDENTIALS", e)
        }
    }
}