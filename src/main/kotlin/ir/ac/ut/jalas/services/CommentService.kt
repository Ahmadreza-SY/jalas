package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.controllers.models.CommentCreationRequest
import ir.ac.ut.jalas.controllers.models.CommentDto
import ir.ac.ut.jalas.exceptions.EntityNotFoundError
import ir.ac.ut.jalas.repositories.CommentRepository
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.stereotype.Service

@Service
class CommentService(val commentRepository: CommentRepository) {

    fun createComment(meetingId: String, owner: String, request: CommentCreationRequest): CommentDto {
        val comment = request.extract(meetingId, owner)
        commentRepository.save(comment)
        return CommentDto(comment)
    }

    fun updateComment(meetingId: String, commentDto: CommentDto): CommentDto {
        val comment = commentDto.extract(meetingId)
        commentRepository.save(comment)
        return CommentDto(comment)
    }

    fun getComments(meetingId: String): List<CommentDto> = commentRepository
            .findByMeetingId(meetingId)
            .sortedByDescending { it.creationDate }
            .map { CommentDto(it) }

    fun deleteComment(commentId: String) {
        val comment = commentRepository.findById(commentId).orElse(null)
                ?: throw EntityNotFoundError(ErrorType.COMMENT_NOT_FOUND)
        commentRepository.delete(comment)
    }

}