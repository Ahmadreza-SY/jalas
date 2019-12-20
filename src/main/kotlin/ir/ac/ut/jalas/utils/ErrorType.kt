package ir.ac.ut.jalas.utils

enum class ErrorType {
    TIMEOUT,

    // 400
    INVALID_DATE_RANGE,
    INVALID_TIME,
    ROOM_ALREADY_RESERVED,
    SLOT_ALREADY_EXISTS,

    // 403
    NOT_MEETING_GUEST,

    // 404
    MEETING_NOT_FOUND,
    SLOT_NOT_FOUND,
    USER_NOT_FOUND,

    // 417
    USER_ALREADY_VOTED,
    USER_NOT_VOTED,

    // 500
    INTERNAL_SERVER_ERROR
}