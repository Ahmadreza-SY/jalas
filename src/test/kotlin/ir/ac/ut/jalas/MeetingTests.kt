package ir.ac.ut.jalas

import ir.ac.ut.jalas.controllers.models.MeetingCreationRequest
import ir.ac.ut.jalas.controllers.models.MeetingResponse
import ir.ac.ut.jalas.controllers.models.VoteRequest
import ir.ac.ut.jalas.entities.nested.TimeRange
import ir.ac.ut.jalas.exceptions.PreconditionFailedError
import ir.ac.ut.jalas.services.MeetingService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class MeetingTests {

    @Autowired
    lateinit var meetingService: MeetingService

    companion object {
        const val MEETING_TITLE = "test_meeting"
        val MEETING_SLOT = TimeRange(0, 10)
        const val MEETING_GUEST = "ahmadreza.saboor2012@gmail.com"
    }

    @Test
    fun shouldCreateMeeting() {
        val response = createMeeting()

        val createdMeeting = meetingService.getMeeting(response.id)

        assert(createdMeeting.title == MEETING_TITLE)
        assert(createdMeeting.slots.size == 1)
    }

    @Test
    fun shouldUpVoteSuccessfully() {
        val meeting = createMeeting()
        val voteRequest = VoteRequest(
                email = MEETING_GUEST,
                slot = MEETING_SLOT,
                agree = true
        )
        meetingService.voteForMeeting(meeting.id, voteRequest)

        val updatedMeeting = meetingService.getMeeting(meeting.id)
        val agreeingUsers = updatedMeeting.slots.first().agreeingUsers

        assert(agreeingUsers.size == 1)
        assert(agreeingUsers.first() == MEETING_GUEST)
    }

    @Test
    fun shouldNotVoteTwice() {
        val meeting = createMeeting()
        val voteRequest = VoteRequest(
                email = MEETING_GUEST,
                slot = MEETING_SLOT,
                agree = false
        )
        meetingService.voteForMeeting(meeting.id, voteRequest)
        assertThrows<PreconditionFailedError> { meetingService.voteForMeeting(meeting.id, voteRequest) }
    }

    private fun createMeeting(): MeetingResponse {
        val request = MeetingCreationRequest(
                title = MEETING_TITLE,
                slots = listOf(MEETING_SLOT),
                guests = listOf(MEETING_GUEST)
        )
        return meetingService.createMeeting(request)
    }

}
