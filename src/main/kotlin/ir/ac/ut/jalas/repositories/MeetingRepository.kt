package ir.ac.ut.jalas.repositories

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface MeetingRepository : MongoRepository<Meeting, String> {
    fun findByStatus(status: MeetingStatus): List<Meeting>
    fun findByOwnerOrGuestsIn(owner: String, guests: List<String>): List<Meeting>
    fun findByOwnerAndStatus(owner: String, status: MeetingStatus): List<Meeting>
    fun findByStatusAndDeadlineBefore(status: MeetingStatus, deadline: Date): List<Meeting>
}