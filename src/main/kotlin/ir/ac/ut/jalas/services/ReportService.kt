package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.controllers.models.reports.GeneralReportResponse
import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.exceptions.AccessDeniedError
import ir.ac.ut.jalas.repositories.MeetingRepository
import ir.ac.ut.jalas.utils.ErrorType
import org.springframework.stereotype.Service

@Service
class ReportService(
        val authService: AuthService,
        val meetingRepository: MeetingRepository
) {

    fun getGeneralReport(): GeneralReportResponse {
        if (!authService.isAdmin())
            throw AccessDeniedError(ErrorType.NOT_ADMIN_USER)
        return GeneralReportResponse(
                reservationTimeAvg = getReservationTimeAvg(),
                reservedRoomsCount = getAllReservedRooms().size,
                canceledMeetingsCount = getCanceledMeetings().size
        )
    }

    fun getReservationTimeAvg(): Double {
        val reservedMeetings = meetingRepository.findByStatus(MeetingStatus.RESERVED)
        return reservedMeetings.mapNotNull { it.reservationTime?.calcDuration() }.average()
    }

    fun getAllReservedRooms(): List<Int> {
        val reservedMeetings = meetingRepository.findByStatus(MeetingStatus.RESERVED)
        return reservedMeetings.mapNotNull { it.roomId }.distinct()
    }

    fun getCanceledMeetings(): List<String> {
        return meetingRepository.findByStatus(MeetingStatus.CANCELED).mapNotNull { it.id }
    }
}