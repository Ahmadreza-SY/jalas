package ir.ac.ut.jalas.entities

import ir.ac.ut.jalas.entities.nested.MeetingPoll
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.MeetingTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "meeting")
data class Meeting(
        @Id
        val id: String? = null,
        val title: String,
        val status: MeetingStatus,
        val time: MeetingTime? = null,
        val votes: List<MeetingPoll> = emptyList(),
        val roomId: Int? = null
)