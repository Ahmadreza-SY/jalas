package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingPoll
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.MeetingTime

data class MeetingResponse(
        val id: String,
        val title: String,
        val status: MeetingStatus,
        val time: MeetingTime?,
        val roomId: Int?,
        val votes: List<MeetingPoll>
) {
    constructor(entity: Meeting) : this(
            entity.id ?: "NA",
            entity.title,
            entity.status,
            entity.time,
            entity.roomId,
            entity.votes
    )
}