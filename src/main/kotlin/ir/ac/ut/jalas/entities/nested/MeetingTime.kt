package ir.ac.ut.jalas.entities.nested

import java.util.*

data class MeetingTime(val start: Date, val end: Date) {

    constructor(start: Long, end: Long) : this(Date(start), Date(end))

}