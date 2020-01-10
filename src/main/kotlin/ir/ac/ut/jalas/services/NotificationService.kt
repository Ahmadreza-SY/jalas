package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.controllers.models.meetings.VoteRequest
import ir.ac.ut.jalas.entities.Meeting
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.NotificationType
import ir.ac.ut.jalas.utils.TemplateEngine
import ir.ac.ut.jalas.utils.toReserveFormat
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class NotificationService(
        val mailService: MailService,
        @Value("\${jalas.dashboard.url}") val dashboardUrl: String
) {

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
        mailService.sendMail(
                subject = "New Vote for Meeting: ${meeting.title}",
                message = """
                            |Dear user,
                            |
                            |User with email: ${request.email} has voted for ${request.vote.name} for time slot ${request.slot.start.toReserveFormat()}-${request.slot.end.toReserveFormat()}
                            |To view more info about the meeting, click on link bellow:
                            |$dashboardUrl/meeting/${meeting.id}
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                to = meeting.owner,
                type = NotificationType.MEETING_VOTE
        )
    }

    fun notifyGuest(meeting: Meeting, owner: User, guest: String) {
        mailService.sendMail(
                subject = "Meeting ${meeting.title} Invitation",
                message = """
                            |Dear Guest,
                            |
                            |You have invited to '${meeting.title}' meeting created by ${owner.fullName()}.
                            |Please visit the following link to vote your available time:
                            |$dashboardUrl/meeting/${meeting.id}/vote/$guest
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
                to = guest,
                type = NotificationType.MEETING_INVITATION
        )
    }

    fun notifyGuestRemoval(meeting: Meeting, owner: User, guest: String) {
        mailService.sendMail(
                subject = "Meeting ${meeting.title} Removal",
                message = """
                            |Dear Guest,
                            |
                            |You have removed from '${meeting.title}' meeting created by ${owner.fullName()}.
                            |We hope seeing you in other meetings
                            |
                            |Best Regards,
                            |Jalas Team
                        """.trimMargin(),
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