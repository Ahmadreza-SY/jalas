package ir.ac.ut.jalas.entities.nested


data class TimeSlot(
        val agreeingUsers: MutableList<String>,
        val disagreeingUsers: MutableList<String>,
        val agreeIfNeededUsers: MutableList<String> = mutableListOf(),
        val time: TimeRange
)