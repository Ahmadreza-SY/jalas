package ir.ac.ut.jalas.configurations

import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.Serializable
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint, Serializable {
    override fun commence(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authException: AuthenticationException
    ) {
        when (authException) {
            is BadCredentialsException -> response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorType.INVALID_CREDENTIALS.name)
            else -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorType.UNAUTHORIZED.name)
        }
    }

    companion object {
        private const val serialVersionUID = -7858869558953243875L
    }
}