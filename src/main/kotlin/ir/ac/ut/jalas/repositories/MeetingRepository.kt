package ir.ac.ut.jalas.repositories

import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface MeetingRepository: MongoRepository<Meeting, String> {
    fun findByStatus(status: MeetingStatus): List<Meeting>
}