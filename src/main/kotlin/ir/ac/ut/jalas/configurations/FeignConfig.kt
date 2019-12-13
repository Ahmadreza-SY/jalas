package ir.ac.ut.jalas.configurations

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["ir.ac.ut.jalas.clients"])
class FeignConfig