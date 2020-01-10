package ir.ac.ut.jalas.jobs

import ir.ac.ut.jalas.controllers.models.meetings.MeetingCreationRequest
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.entities.nested.NotificationType
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.repositories.UserRepository
import ir.ac.ut.jalas.services.MeetingService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random


@Component
class DBSeeder(
        val meetingService: MeetingService,
        val userRepository: UserRepository,
        val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)
    private val userEmails = listOf(
            "mohammad.76kiani@gmail.com",
            "mohammad_76kiani@yahoo.com",
            "ahmadreza.saboor2012@gmail.com",
            "shahryar.soltanpour@gmail.com",
            "no.replay.jalas@gmail.com"
    )

    private fun createUsers() {
        val password = passwordEncoder.encode("password")
        val allNotifications = NotificationType.values().toList()
        val users = listOf(
                User(
                        email = userEmails[0],
                        firstName = "Admin",
                        lastName = "",
                        password = password,
                        notificationTypes = allNotifications
                ),
                User(
                        email = userEmails[1],
                        firstName = "MohammadReza",
                        lastName = "Kiani",
                        password = password,
                        notificationTypes = allNotifications
                ),
                User(
                        email = userEmails[2],
                        firstName = "Ahmadreza",
                        lastName = "Saboor",
                        password = password,
                        notificationTypes = allNotifications
                ),
                User(
                        email = userEmails[3],
                        firstName = "Shahryar",
                        lastName = "Soltanpour",
                        password = password,
                        notificationTypes = allNotifications
                ),
                User(
                        email = "ramtung@gmail.com",
                        firstName = "Ramtin",
                        lastName = "Khosravi",
                        password = password,
                        notificationTypes = allNotifications
                )
        )
        userRepository.deleteAll()
        userRepository.saveAll(users)
        logger.info("created sample users")
    }

    private fun createMeetings() {
        (1..10)
                .map { index -> makeRandomMeetingCreationRequest(index) }
                .onEach { request -> meetingService.createMeeting(request) }
        logger.info("Added 10 random meetings")
    }

    private fun login() {
        val user = userRepository.findByEmail("mohammad.76kiani@gmail.com")
        val auth = UsernamePasswordAuthenticationToken(user, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    override fun run(vararg args: String?) {
        createUsers()
//        login()
//        createMeetings()
    }

    private fun makeRandomMeetingCreationRequest(index: Int): MeetingCreationRequest {
        return MeetingCreationRequest(
                title = "Meeting $index",
                slots = (0..5).map { makeRandomTimeSlot() },
                guests = emptyList(),
                deadline = null
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
        val votes = Random.nextInt(from = 1, until = userEmails.size)
        return userEmails.shuffled().take(votes)
    }

}