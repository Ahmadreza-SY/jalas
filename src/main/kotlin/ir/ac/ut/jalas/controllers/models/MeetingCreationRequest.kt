package ir.ac.ut.jalas.controllers.models

import ir.ac.ut.jalas.entities.nested.MeetingTime

data class MeetingCreationRequest(
        val meetingId: String,
        val selectedTime: MeetingTime,
        val selectedRoom: Int
)