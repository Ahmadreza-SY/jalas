package ir.ac.ut.jalas.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
class InternalServerError(message: String?) : RuntimeException(message)