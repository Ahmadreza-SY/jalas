package ir.ac.ut.jalas.entities

import ir.ac.ut.jalas.entities.nested.TimeSlot
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "meeting")
data class Meeting(
        @Id
        var id: String? = null,
        val title: String,
        var status: MeetingStatus,
        var time: TimeRange? = null,
        val slots: MutableList<TimeSlot> = mutableListOf(),
        var roomId: Int? = null,
        val owner: String,
        var reservationTime: TimeRange? = null,
        val guests: List<String> = emptyList()
)