package ir.ac.ut.jalas.entities.nested


data class TimeSlot(
        val agreeingUsers: List<String>,
        val disagreeingUsers: List<String>,
        val time: TimeRange
)