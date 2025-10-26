package com.example.sgr

import com.github.victools.jsonschema.generator.OptionPreset.PLAIN_JSON
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_2020_12
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.github.victools.jsonschema.module.jackson.JacksonOption.RESPECT_JSONPROPERTY_REQUIRED
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SchemaBuilder() {
    private val logger = LoggerFactory.getLogger(SchemaBuilder::class.java)

    fun generateSchema(modelClass: Class<NextStep>): String =
        SchemaGeneratorConfigBuilder(DRAFT_2020_12, PLAIN_JSON)
            .with(JacksonModule(RESPECT_JSONPROPERTY_REQUIRED))
            .build()
            .let { SchemaGenerator(it) }
            .generateSchema(modelClass)
            .toPrettyString()
            .also { logger.info("Generated JSON Schema: \n$it") }
}
