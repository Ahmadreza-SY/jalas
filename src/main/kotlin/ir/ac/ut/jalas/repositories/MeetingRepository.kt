package ir.ac.ut.jalas.repositories

import ir.ac.ut.jalas.entities.Meeting
import org.springframework.data.mongodb.repository.MongoRepository

interface MeetingRepository: MongoRepository<Meeting, String>