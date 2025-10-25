package com.example.sgr

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Bean
    fun openAiChatModel(
        @Value("\${spring.ai.openai.api-key}") apiKey: String
    ): OpenAiChatModel {
        val openAiApi = OpenAiApi(apiKey)
        val options = OpenAiChatOptions.builder()
            .withModel("gpt-4o-mini")
            .withTemperature(0.0)
            .build()
        return OpenAiChatModel(openAiApi, options)
    }
}
