package ir.ac.ut.jalas.entities

import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.entities.nested.TimeSlot
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

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
        val guests: MutableList<String> = mutableListOf(),
        val deadline: Date? = null
) {
    fun isParticipant(userEmail: String): Boolean =
            guests.contains(userEmail.toLowerCase()) || owner == userEmail.toLowerCase()
}