package ir.ac.ut.jalas.entities.nested

import ir.ac.ut.jalas.controllers.models.meetings.VoteOption
import ir.ac.ut.jalas.exceptions.PreconditionFailedError
import ir.ac.ut.jalas.utils.ErrorType


data class TimeSlot(
        val agreeingUsers: MutableList<String>,
        val disagreeingUsers: MutableList<String>,
        val agreeIfNeededUsers: MutableList<String> = mutableListOf(),
        val time: TimeRange
) {

    fun addVote(vote: VoteOption, email: String) {
        when (vote) {
            VoteOption.AGREE -> {
                addVote(agreeingUsers, email)
                removeVote(email, disagreeingUsers, agreeIfNeededUsers)
            }
            VoteOption.DISAGREE -> {
                addVote(disagreeingUsers, email)
                removeVote(email, agreeIfNeededUsers, agreeingUsers)
            }
            VoteOption.AGREE_IF_NEEDED -> {
                addVote(agreeIfNeededUsers, email)
                removeVote(email, agreeingUsers, disagreeingUsers)
            }
            VoteOption.REVOKE -> {
                revokeVote(email)
            }
        }
    }

    private fun revokeVote(email: String) {
        when {
            agreeingUsers.contains(email) -> agreeingUsers -= email
            disagreeingUsers.contains(email) -> disagreeingUsers -= email
            agreeIfNeededUsers.contains(email) -> agreeIfNeededUsers -= email
            else -> throw PreconditionFailedError(ErrorType.USER_NOT_VOTED)
        }
    }

    private fun addVote(listToAdd: MutableList<String>, email: String) {
        if (listToAdd.contains(email))
            throw PreconditionFailedError(ErrorType.USER_ALREADY_VOTED)
        listToAdd.add(email)
    }

    private fun removeVote(email: String, vararg listsToRemove: MutableList<String>) {
        listsToRemove.forEach { list -> list.removeIf { voter -> voter == email } }
    }

}