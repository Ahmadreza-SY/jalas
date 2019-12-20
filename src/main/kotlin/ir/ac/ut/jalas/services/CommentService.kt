package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.controllers.models.CommentCreationRequest
import ir.ac.ut.jalas.controllers.models.CommentResponse
import ir.ac.ut.jalas.repositories.CommentRepository
import org.springframework.stereotype.Service

@Service
class CommentService(val commentRepository: CommentRepository) {

    fun createComment(meetingId: String, owner: String, request: CommentCreationRequest): CommentResponse {
        val comment = request.extract(meetingId, owner)
        commentRepository.save(comment)
        return CommentResponse(comment)
    }

    fun getComments(meetingId: String): List<CommentResponse> = commentRepository
            .findByMeetingId(meetingId)
            .sortedByDescending { it.creationDate }
            .map { CommentResponse(it) }

}