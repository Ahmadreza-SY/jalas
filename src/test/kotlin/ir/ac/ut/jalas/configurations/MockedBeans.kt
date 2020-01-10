package ir.ac.ut.jalas.configurations

import ir.ac.ut.jalas.clients.ReservationClient
import ir.ac.ut.jalas.entities.User
import ir.ac.ut.jalas.services.AuthService
import ir.ac.ut.jalas.services.MailService
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile


@Profile("test")
@Configuration
class MockedBeans {
    @Bean
    @Primary
    fun mailService(): MailService {
        return Mockito.mock(MailService::class.java)
    }

    @Bean
    @Primary
    fun reservationClient(): ReservationClient {
        return Mockito.mock(ReservationClient::class.java)
    }

    @Bean
    @Primary
    fun authService(): AuthService {
        val mock = Mockito.mock(AuthService::class.java)
        Mockito.`when`(mock.getLoggedInUser()).thenReturn(User(
                id = "NA",
                email = "mohammad.76kiani@gmail.com",
                firstName = "Mohammad Reza",
                lastName = "Kiani",
                password = "password hash",
                notificationTypes = emptyList()
        ))
        return mock
    }
}