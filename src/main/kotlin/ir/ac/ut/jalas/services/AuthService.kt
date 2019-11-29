package ir.ac.ut.jalas.services

import ir.ac.ut.jalas.entities.User
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service

@Service
class AuthService {

    fun getLoggedInUser() = User(
            id = "1",
            email = "mohammad.76kiani@gmail.com",
            firstName = "Mohammad Reza",
            lastName = "Kiani",
            password = DigestUtils.sha1Hex("my-pass")
    )

}

