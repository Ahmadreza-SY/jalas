package ir.ac.ut.jalas.controllers.models.reports

data class GeneralReportResponse(
        val reservationTimeAvg: Double,
        val reservedRoomsCount: Int,
        val canceledMeetingsCount: Int
)