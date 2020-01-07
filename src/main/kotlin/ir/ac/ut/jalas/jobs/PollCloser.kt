package ir.ac.ut.jalas.jobs

import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.repositories.MeetingRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*


@Component
class PollCloser(val meetingRepository: MeetingRepository) {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    @Scheduled(fixedDelay = 30 * 1000L)
    fun closePollsIfDeadlinePassed() {
        val now = Date()
        val toBeClosedMeetings = meetingRepository.findByStatusAndDeadlineBefore(MeetingStatus.ELECTING, now)
        toBeClosedMeetings.onEach { it.status = MeetingStatus.CLOSED }
        meetingRepository.saveAll(toBeClosedMeetings)

        logger.info("closed ${toBeClosedMeetings.size} electing meeting with passed deadline")
    }
}