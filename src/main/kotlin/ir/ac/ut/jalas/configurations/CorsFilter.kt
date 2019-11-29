package ir.ac.ut.jalas.configurations


import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val response = res as HttpServletResponse
        val request = req as HttpServletRequest
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, OPTIONS, DELETE")
        response.setHeader("Access-Control-Allow-Headers", "x-auth-token, x-requested-with, content-type, accept, origin, referer, Authorization, responseType, response-type")
        response.setHeader("Access-Control-Expose-Headers", "x-requested-with")
        if (request.method != "OPTIONS") {
            chain.doFilter(req, res)
        }
    }

    override fun init(filterConfig: FilterConfig?) {}

    override fun destroy() {}

}
