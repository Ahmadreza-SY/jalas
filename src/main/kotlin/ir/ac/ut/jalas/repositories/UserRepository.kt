package ir.ac.ut.jalas.repositories

import ir.ac.ut.jalas.entities.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}