package com.example.sgr

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import com.openai.client.OpenAI
import com.openai.models.ChatCompletionCreateParams
import com.openai.models.ChatCompletionMessageParam
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value

@Service
class SchemaGuidedReasoning(
    dbStub: DatabaseStub,
    schemaBuilder: SchemaBuilder,
    private val toolsDispatcherStub: ToolDispatcherStub,
    private val objectMapper: ObjectMapper,
    private val openAi: OpenAI,
    @Value("\${openai.model}") private val model: String,
    @Value("\${openai.temperature:1.0}") private val temperature: Double
) {
    private val logger = LoggerFactory.getLogger(SchemaGuidedReasoning::class.java)
    private val promptResponseSchema = schemaBuilder.generateSchema(NextStep::class.java)

    private val systemPrompt = """
        You are a business assistant helping Sergei with customer interactions.

        - Clearly report when tasks are done.
        - Always send customers emails after issuing invoices (with invoice attached).
        - Be concise. Especially in emails
        - No need to wait for payment confirmation before proceeding.
        - Always check customer data before issuing invoices or making changes.
        - List of available products: ${dbStub.products}
        - Respond with ONLY valid JSON, no additional text.
        - Your response must be a JSON object following this schema:
         $promptResponseSchema        
    """.trimIndent()

    fun executeTasks(tasks: List<String>) {
        tasks.forEach { task ->
            executeTask(task)
        }
    }

    private fun executeTask(task: String) {
        logger.info("Launch agent with task: $task")

        val agentDialog = mutableListOf<ChatEntry>(
            ChatEntry.System(systemPrompt),
            ChatEntry.User(task)
        )

        for (i in 1..20) {
            val step = "step_$i"
            logger.info("Planning $step... ")

            val nextStep = requestNextStep(agentDialog)

            if (nextStep.isTaskCompleted()) {
                logCompletion(nextStep)
                return
            }

            val nextStepPlan = nextStep.planRemainingStepsBrief.firstOrNull() ?: "No plan"
            agentDialog.add(buildToolCallEntry(nextStepPlan, step, nextStep))

            val toolResponse = toolsDispatcherStub.dispatch(nextStep.function)
            agentDialog.add(buildToolResponseEntry(step, nextStep, toolResponse))
        }

        logger.error("Max iterations reached. Aborting execution.")
    }

    private fun requestNextStep(agentDialog: List<ChatEntry>): NextStep {
        val completion = openAi.chat().completions().create(
            ChatCompletionCreateParams.builder()
                .model(model)
                .messages(toChatMessages(agentDialog))
                .temperature(temperature)
                .build()
        )

        val assistantReply = completion.choices()
            .firstOrNull()
            ?.message()
            ?.content()
            ?.joinToString(separator = "") { content ->
                content.text()
                    .map { it.value() }
                    .orElse("")
            }
            ?.takeIf { it.isNotBlank() }
            ?: error("No content returned from OpenAI")

        return objectMapper.readValue(assistantReply, NextStep::class.java)
    }

    private fun logCompletion(nextStep: NextStep) {
        logger.info("Task completed. Summary:")
        (nextStep.function as ReportTaskCompletion).completedStepsLaconic.forEach { step ->
            logger.info("- $step")
        }
    }

    private fun buildToolCallEntry(
        nextStepPlan: String,
        step: String,
        nextStep: NextStep,
    ): ChatEntry.Assistant =
        objectMapper.writeValueAsString(nextStep.function)
            .also { logger.info("Next step: $nextStepPlan") }
            .also { logger.info("Next tool call: \n$it") }
            .let { payload ->
                ChatEntry.Assistant(
                    "TOOL_CALL [$step] ${nextStep.function.tool}: $payload"
                )
            }

    private fun buildToolResponseEntry(
        step: String,
        nextStep: NextStep,
        result: String
    ): ChatEntry.User =
        objectMapper.readValue(result, Any::class.java)
            .let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it)}
            .also { logger.info("Tool response: \n$it") }
            .let { formatted ->
                ChatEntry.User("TOOL_RESPONSE [$step] ${nextStep.function.tool}: $formatted")
            }

    private fun toChatMessages(agentDialog: List<ChatEntry>): List<ChatCompletionMessageParam> =
        agentDialog.map {
            when (it) {
                is ChatEntry.System -> ChatCompletionMessageParam.ofSystem(it.content)
                is ChatEntry.User -> ChatCompletionMessageParam.ofUser(it.content)
                is ChatEntry.Assistant -> ChatCompletionMessageParam.ofAssistant(it.content)
            }
        }

    private sealed interface ChatEntry {
        val content: String

        data class System(override val content: String) : ChatEntry
        data class User(override val content: String) : ChatEntry
        data class Assistant(override val content: String) : ChatEntry
    }

}
