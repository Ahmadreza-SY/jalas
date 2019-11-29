package ir.ac.ut.jalas.utils

import feign.FeignException
import org.springframework.http.HttpStatus

fun FeignException.extractErrorMessage(): String? {
    return when {
        status() == 0 -> ErrorType.TIMEOUT.toString()
        status() == HttpStatus.BAD_REQUEST.value() -> ErrorType.ROOM_ALREADY_RESERVED.toString()
        status() == HttpStatus.INTERNAL_SERVER_ERROR.value() -> ErrorType.INTERNAL_SERVER_ERROR.toString()
        else -> message
    }
}

