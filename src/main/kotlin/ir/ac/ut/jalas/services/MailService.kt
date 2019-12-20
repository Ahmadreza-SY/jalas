package ir.ac.ut.jalas.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class MailService(
        private val javaMailSender: JavaMailSender,
        @Value("\${jalas.notificationEnabled}")
        val notificationEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)


    @Async
    fun sendMail(subject: String, message: String, to: String) {
        if (!notificationEnabled)
            return
        val mailMessage = SimpleMailMessage()
        mailMessage.setTo(to)
        mailMessage.setSubject(subject)
        mailMessage.setText(message)

        javaMailSender.send(mailMessage)

        logger.info("email '$subject' successfully sent to '$to'")
    }
}