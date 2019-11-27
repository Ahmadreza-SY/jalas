package ir.ac.ut.jalas.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.NOT_FOUND)
class EntityNotFoundError(message: String?) : RuntimeException(message)