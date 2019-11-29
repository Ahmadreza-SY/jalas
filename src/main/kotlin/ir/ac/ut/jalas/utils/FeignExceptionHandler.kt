package ir.ac.ut.jalas.utils

import feign.FeignException
import org.springframework.http.HttpStatus

fun FeignException.extractErrorMessage(): String? {
    return when {
        status() == 0 -> ErrorType.TIMEOUT.toString()
        status() == HttpStatus.BAD_REQUEST.value() -> ErrorType.ROOM_ALREADY_RESERVED.toString()
        status() in 500..599 -> ErrorType.INTERNAL_SERVER_ERROR.toString()
        else -> message
    }
}

