package ir.ac.ut.jalas.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException

fun FeignException.extractErrorMessage(): String? {
    var errorBody = message

    val pattern = "([^{]*)([^}]*)}"
    errorBody = errorBody?.replace("\n".toRegex(), "")
    errorBody = errorBody?.replace(pattern.toRegex(), "$2") + "}"

    return try {
        val mapper = ObjectMapper()
        val map = mapper.readValue(errorBody, object : TypeReference<Map<String, String>>() {

        })
        map["message"]
    } catch (e: Exception) {
        message
    }
}

