package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.controllers.models.meetings.VoteRequest
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.NotificationType
import ir.ac.ut.jalas.entities.nested.TimeSlot
import ir.ac.ut.jalas.utils.TemplateEngine
import ir.ac.ut.jalas.utils.toReserveFormat
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class NotificationService(
        val mailService: MailService,
        @Value("\${jalas.dashboard.url}") val dashboardUrl: String
) {

    fun notifyPollOptionDeletion(deletedSlot: TimeSlot, meeting: Meeting) {
        val renderMap = mutableMapOf(
                "meetingTitle" to meeting.title,
                "landingUrl" to "$dashboardUrl/meeting/${meeting.id}"
        )
        deletedSlot.getAllVoters().forEach { voter ->
            renderMap["startTime"] = deletedSlot.time.start.toString()
            renderMap["endTime"] = deletedSlot.time.end.toString()
            val message = TemplateEngine.render("poll-option-deletion", renderMap)
            mailService.sendMail(
                    subject = "Meeting Time Option Removal",
                    message = message,
                    to = voter,
                    type = NotificationType.MEETING_TIME_OPTION_CHANGE
            )
        }
    }

    fun notifySuccessReservation(user: User, meeting: Meeting) {
        (meeting.guests + meeting.owner).forEach { participant ->
            val participantName = if (participant == meeting.owner) user.firstName else "Participant"
            val renderMap = mapOf(
                    "participantName" to participantName,
                    "meetingTitle" to meeting.title,
                    "meetingStartTime" to meeting.time?.start.toString(),
                    "meetingEndTime" to meeting.time?.end.toString(),
                    "meetingRoomId" to meeting.roomId.toString(),
                    "landingUrl" to "$dashboardUrl/meeting/${meeting.id}"
            )
            val message = TemplateEngine.render("success-reservation", renderMap)
            mailService.sendMail(
                    subject = "Meeting Reservation Success",
                    message = message,
                    to = participant,
                    type = NotificationType.MEETING_RESERVATION
            )
        }
    }

    fun sendNewVoteNotification(request: VoteRequest, meeting: Meeting) {
        val renderMap = mapOf(
                "voterEmail" to request.email,
                "voteName" to request.vote.name,
                "slotStart" to request.slot.start.toReserveFormat(),
                "slotEnd" to request.slot.end.toReserveFormat(),
                "landingUrl" to "$dashboardUrl/meeting/${meeting.id}"
        )
        val message = TemplateEngine.render("new-vote", renderMap)
        mailService.sendMail(
                subject = "New Vote for Meeting: ${meeting.title}",
                message = message,
                to = meeting.owner,
                type = NotificationType.MEETING_VOTE
        )
    }

    fun notifyGuest(meeting: Meeting, owner: User, guest: String) {
        val renderMap = mapOf(
                "meetingTitle" to meeting.title,
                "ownerFullName" to owner.fullName(),
                "landingUrl" to "$dashboardUrl/meeting/${meeting.id}/vote/$guest"
        )
        val message = TemplateEngine.render("meeting-invitation", renderMap)
        mailService.sendMail(
                subject = "Meeting ${meeting.title} Invitation",
                message = message,
                to = guest,
                type = NotificationType.MEETING_INVITATION
        )
    }

    fun notifyGuestRemoval(meeting: Meeting, owner: User, guest: String) {
        val renderMap = mapOf(
                "meetingTitle" to meeting.title,
                "ownerFullName" to owner.fullName()
        )
        val message = TemplateEngine.render("guest-removal", renderMap)
        mailService.sendMail(
                subject = "Meeting ${meeting.title} Removal",
                message = message,
                to = guest,
                type = NotificationType.MEETING_REMOVE_GUEST
        )
    }

    fun notifyGuests(meeting: Meeting, owner: User) {
        meeting.guests.onEach { guest ->
            notifyGuest(meeting, owner, guest)
        }
    }

}