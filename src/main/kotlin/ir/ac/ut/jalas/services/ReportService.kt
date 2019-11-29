package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.entities.nested.MeetingStatus
import ir.ac.ut.jalas.repositories.MeetingRepository
import org.springframework.stereotype.Service

@Service
class ReportService(val meetingRepository: MeetingRepository) {

    fun getAllReservedRooms(): List<Int> {
        val reservedMeetings = meetingRepository.findByStatus(MeetingStatus.RESERVED)
        return reservedMeetings.mapNotNull { it.roomId }
    }

    fun getCanceledMeetings(): List<String> {
        return meetingRepository.findByStatus(MeetingStatus.CANCELED).mapNotNull { it.id }
    }
}