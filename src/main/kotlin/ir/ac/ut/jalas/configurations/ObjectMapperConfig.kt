package ir.ac.ut.jalas.configurations

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ObjectMapperConfig {

    companion object {
        val modules = listOf(KotlinModule(), Jdk8Module(), JavaTimeModule())
    }

    /**
     * configuration of spring builtin jackson
     */
    @Bean
    @Primary
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { mapper ->
        mapper.modules(modules)
    }

    /**
     * provides configured object mapper for developer usage
     */
    @Bean(name = ["CustomMapper"])
    @Primary
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
                .registerModules(modules)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
    }
}
