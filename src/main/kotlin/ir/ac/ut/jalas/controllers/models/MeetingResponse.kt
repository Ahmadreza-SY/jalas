package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus

data class MeetingResponse(
        val id: String,
        val title: String,
        val status: MeetingStatus,
        val time: TimeRangeResponse?,
        val roomId: Int?,
        val slots: List<TimeSlotResponse>,
        val owner: String
) {
    constructor(entity: Meeting) : this(
            entity.id ?: "NA",
            entity.title,
            entity.status,
            entity.time?.let { TimeRangeResponse(it) },
            entity.roomId,
            entity.slots.map { TimeSlotResponse(it) },
            entity.owner
    )
}