package com.example.sgr

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class AppConfiguration {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
    }

    @Bean
    fun restClientBuilder(
        @Value("\${spring.ai.openai.timeout}") timeout: Long
    ): RestClient.Builder =
        SimpleClientHttpRequestFactory()
            .apply { setReadTimeout(Duration.ofSeconds(timeout)) }
            .let { BufferingClientHttpRequestFactory(it) }
            .let { RestClient.builder().requestFactory(it) }
}
