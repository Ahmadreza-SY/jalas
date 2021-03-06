package ir.ac.ut.jalas.controllers.models.meetings

import ir.ac.ut.jalas.controllers.models.comment.CommentDto
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus

data class MeetingResponse(
        val id: String,
        val title: String,
        val status: MeetingStatus,
        val time: TimeRangeResponse?,
        val roomId: Int?,
        val slots: List<TimeSlotResponse>,
        val guests: List<String>,
        val owner: String,
        var comments: List<CommentDto> = emptyList()
) {
    constructor(entity: Meeting) : this(
            entity.id ?: "NA",
            entity.title,
            entity.status,
            entity.time?.let { TimeRangeResponse(it) },
            entity.roomId,
            entity.slots.map { TimeSlotResponse(it) },
            entity.guests,
            entity.owner
    )

    constructor(entity: Meeting, comments: List<CommentDto>) : this(entity) {
        this.comments = comments
    }
}