package ir.ac.ut.jalas.entities.nested


data class MeetingPoll(
        val agreeingUsers: List<String>,
        val disagreeingUsers: List<String>,
        val time: TimeRange
)