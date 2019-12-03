package ir.ac.ut.jalas.jobs

import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.repositories.MeetingRepository
import ir.ac.ut.jalas.services.MeetingService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReservationChecker(
        val meetingService: MeetingService,
        val meetingRepository: MeetingRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    @Scheduled(fixedDelay = 30 * 1000L)
    fun checkPendingReservations() {
        val pendingMeetings = meetingRepository.findByStatus(MeetingStatus.PENDING)
        logger.info("checkPendingReservations started with ${pendingMeetings.size} records to check")
        pendingMeetings.map {
            it to meetingService.reserveMeeting(
                    meeting = it,
                    selectedRoom = it.roomId!!,
                    selectedTime = it.time!!
            )
        }.onEach { (meeting, message) ->
            if (meeting.status != MeetingStatus.RESERVED)
                logger.error("meeting ${meeting.id} reservation failed with error: $message")
        }
    }
}