package ir.ac.ut.jalas.entities.nested

import java.util.*

data class TimeRange(val start: Date, val end: Date) {

    constructor(start: Long, end: Long) : this(Date(start), Date(end))

    fun notValid() = start > end

    fun calcDuration() = end.time - start.time

    override fun equals(other: Any?): Boolean {
        if (other != null && other is TimeRange)
            return start == other.start && end == other.end
        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}