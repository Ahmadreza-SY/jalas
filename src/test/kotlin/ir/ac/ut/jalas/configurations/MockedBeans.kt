package ir.ac.ut.jalas.configurations

import ir.ac.ut.jalas.clients.ReservationClient
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
}