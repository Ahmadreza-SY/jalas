package ir.ac.ut.jalas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
class JalasApplication

fun main(args: Array<String>) {
	runApplication<JalasApplication>(*args)
}
