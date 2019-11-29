package ir.ac.ut.jalas.utils

enum class ErrorType {
    TIMEOUT,

    // 400
    INVALID_DATE_RANGE,
    INVALID_TIME,
    ROOM_ALREADY_RESERVED,

    // 404
    MEETING_NOT_FOUND,

    // 500
    INTERNAL_SERVER_ERROR
}