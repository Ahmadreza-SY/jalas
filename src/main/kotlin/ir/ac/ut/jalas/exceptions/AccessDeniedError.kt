package ir.ac.ut.jalas.exceptions

import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.FORBIDDEN)
class AccessDeniedError(errorType: ErrorType) : RuntimeException(errorType.toString())