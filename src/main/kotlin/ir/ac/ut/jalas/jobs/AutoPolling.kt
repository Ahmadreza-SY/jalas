package ir.ac.ut.jalas.jobs

import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.repositories.MeetingRepository
import ir.ac.ut.jalas.services.MeetingService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class AutoPolling(
        val meetingService: MeetingService,
        val meetingRepository: MeetingRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    @Scheduled(fixedDelay = 30 * 1000L)
    fun autoPoll() {
        val toBeReservedMeetings = meetingRepository.findByStatus(MeetingStatus.CLOSED)
        logger.info("selected ${toBeReservedMeetings.size} meetings to perform auto poll on")

        toBeReservedMeetings.onEach { meeting ->
            if (meeting.slots.isEmpty()) {
                meeting.status = MeetingStatus.CANCELED
                meetingRepository.save(meeting)

                logger.info("canceling meeting[id: ${meeting.id}] with no slots")
            } else {
                meeting.slots.sortBy { it.agreeingUsers.size }
                val bestSlot = meeting.slots.first()
                val selectedRoom = tryUntilSuccess {
                    meetingService.getAvailableRooms(bestSlot.time)
                }.availableRooms.random()
                meeting.reservationTime = TimeRange(Date(), Date())
                meetingService.reserveMeeting(meeting, selectedRoom, bestSlot.time)

                logger.info("reserving meeting[id: ${meeting.id}] for slot ${bestSlot.time} on room $selectedRoom")
            }
        }
    }

    private fun <T> tryUntilSuccess(op: () -> T): T {
        while (true) {
            try {
                return op()
            } catch (e: Exception) {
                logger.info("failed to get result... retrying in 1000ms")
                Thread.sleep(1000)
            }
        }
    }
}