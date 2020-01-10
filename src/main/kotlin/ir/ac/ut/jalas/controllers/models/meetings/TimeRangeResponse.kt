package ir.ac.ut.jalas.controllers.models.meetings

import ir.ac.ut.jalas.entities.nested.TimeRange

data class TimeRangeResponse(
        val start: Long,
        val end: Long
) {
    constructor(entity: TimeRange) : this(entity.start.time, entity.end.time)
}