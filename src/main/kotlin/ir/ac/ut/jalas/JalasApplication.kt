package ir.ac.ut.jalas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.feign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class JalasApplication

fun main(args: Array<String>) {
	runApplication<JalasApplication>(*args)
}
