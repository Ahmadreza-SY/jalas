package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.repositories.MeetingRepository
import org.springframework.stereotype.Service

@Service
class ReportService(val meetingRepository: MeetingRepository) {

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