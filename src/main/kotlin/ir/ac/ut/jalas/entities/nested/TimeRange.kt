package ir.ac.ut.jalas.entities.nested

import java.util.*

data class TimeRange(val start: Date, val end: Date) {

    constructor(start: Long, end: Long) : this(Date(start), Date(end))

    fun notValid() = start > end

    fun calcDuration() = end.time - start.time
}