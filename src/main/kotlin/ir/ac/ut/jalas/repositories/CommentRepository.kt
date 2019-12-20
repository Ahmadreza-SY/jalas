package ir.ac.ut.jalas.repositories

import ir.ac.ut.jalas.entities.Comment
import org.springframework.data.mongodb.repository.MongoRepository

interface CommentRepository : MongoRepository<Comment, String> {

    fun findByMeetingId(meetingId: String): List<Comment>

}