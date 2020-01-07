package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.nested.TimeSlot

data class TimeSlotResponse(
        val agreeingUsers: List<String>,
        val disagreeingUsers: List<String>,
        val agreeIfNeededUsers: List<String>,
        val time: TimeRangeResponse
) {
    constructor(entity: TimeSlot) : this(
            entity.agreeingUsers,
            entity.disagreeingUsers,
            entity.agreeIfNeededUsers,
            TimeRangeResponse(entity.time)
    )
}