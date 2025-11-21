package com.example.sgr

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.openai.client.OpenAI
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    fun openAiClient(
        @Value("\${openai.api-key}") apiKey: String,
        @Value("\${openai.base-url:https://api.openai.com/v1}") baseUrl: String,
        @Value("\${openai.timeout-seconds:30}") timeoutSeconds: Long
    ): OpenAI {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(timeoutSeconds))
            .build()

        return OpenAI.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .httpClient(httpClient)
            .build()
    }
}
