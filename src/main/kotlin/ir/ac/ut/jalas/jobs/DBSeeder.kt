package ir.ac.ut.jalas.jobs

import ir.ac.ut.jalas.controllers.models.MeetingCreationRequest
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.services.MeetingService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random


@Component
class DBSeeder(val meetingService: MeetingService) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)
    private val users = listOf(
            "mohammad.76kiani@gmail.com",
            "mohammad_76kiani@yahoo.com",
            "ahmadreza.saboor2012@gmail.com",
            "shahryar.soltanpour@gmail.com",
            "no.replay.jalas@gmail.com"
    )

    override fun run(vararg args: String?) {
        (1..10)
                .map { index -> makeRandomMeetingCreationRequest(index) }
                .onEach { request -> meetingService.createMeeting(request) }

        logger.info("Added 10 random meetings")
    }

    private fun makeRandomMeetingCreationRequest(index: Int): MeetingCreationRequest {
        return MeetingCreationRequest(
                title = "Meeting $index",
                slots = (0..5).map { makeRandomTimeSlot() },
                guests = emptyList()
        )
    }

    private fun makeRandomTimeSlot(): TimeRange {
        val now = Date().time
        val start = DateTime(Random.nextLong(from = now, until = now + Duration.of(2, ChronoUnit.DAYS).toMillis()))
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(Random.nextInt(until = 2) * 30)
                .toDate()
                .time
        val end = start + Random.nextInt(from = 1, until = 4) * Duration.of(1, ChronoUnit.HOURS).toMillis()
        return TimeRange(start, end)
    }

    private fun getRandomEmails(): List<String> {
        val votes = Random.nextInt(from = 1, until = users.size)
        return users.shuffled().take(votes)
    }

}