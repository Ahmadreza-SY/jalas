package ir.ac.ut.jalas.mongo

import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MongoConfig {
    @Bean
    fun mongoTemplate(): MongoTemplate {
        val mongo = EmbeddedMongoFactoryBean()
        mongo.setBindIp(MONGO_DB_URL)
        val mongoClient = mongo.getObject()
        return MongoTemplate(mongoClient!!, MONGO_DB_NAME)
    }

    companion object {
        private const val MONGO_DB_URL = "localhost"
        private const val MONGO_DB_NAME = "jalas"
    }
}