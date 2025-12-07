package com.example.sgr

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.github.victools.jsonschema.generator.Option
import com.github.victools.jsonschema.generator.OptionPreset.PLAIN_JSON
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.github.victools.jsonschema.module.jackson.JacksonOption
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SchemaBuilder() {
    private val logger = LoggerFactory.getLogger(SchemaBuilder::class.java)

    fun <T> generateSchema(modelClass: Class<T>): String =
        SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, PLAIN_JSON)
            .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
            .with(Option.INLINE_ALL_SCHEMAS)
            .with(JacksonModule(JacksonOption.IGNORE_TYPE_INFO_TRANSFORM))
            .also { config ->
                // Mark all fields as required
                config.forFields().withRequiredCheck { true }

                // Turn fields annotated with @ToolName into a const
                config.forFields()
                    .withInstanceAttributeOverride { attrs, scope, _ ->
                        scope.getAnnotation(ToolName::class.java)
                            ?.value
                            ?.also { attrs.set<ObjectNode>("const", TextNode(it)) }
                    }
            }
            .build()
            .let { SchemaGenerator(it) }
            .generateSchema(modelClass)
            .toPrettyString()
            .also { logger.info("<SCHEMA_START>\n$it<SCHEMA_END>") }

}
