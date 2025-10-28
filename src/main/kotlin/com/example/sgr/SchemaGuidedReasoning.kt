package com.example.sgr

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.ToolResponseMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.stereotype.Service

@Service
class SchemaGuidedReasoning(
    dbStub: DatabaseStub,
    schemaBuilder: SchemaBuilder,
    private val toolsDispatcherStub: ToolDispatcherStub,
    private val objectMapper: ObjectMapper,
    private val chatModel: OpenAiChatModel,
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

        val agentDialog = mutableListOf<Message>(
            SystemMessage(systemPrompt),
            UserMessage(task)
        )

        for (i in 1..20) {
            val step = "step_$i"
            logger.info("Planning $step... ")

            val nextStep = chatModel.call(Prompt(agentDialog))
                .result.output.text
                .let { objectMapper.readValue(it, NextStep::class.java) }

            if (nextStep.isTaskCompleted()) {
                logCompletion(nextStep)
                return
            }

            val nextStepPlan = nextStep.planRemainingStepsBrief.firstOrNull() ?: "No plan"
            agentDialog.add(buildToolCallMessage(nextStepPlan, step, nextStep))
            logger.info("Next step: $nextStepPlan. Next tool call: ${nextStep.function.tool}")

            val toolResponse = toolsDispatcherStub.dispatch(nextStep.function)
            agentDialog.add(buildToolResponseMessage(step, nextStep, toolResponse))
        }

        logger.error("Max iterations reached. Aborting execution.")
    }

    private fun logCompletion(nextStep: NextStep) {
        logger.info("Task completed. Summary:")
        (nextStep.function as ReportTaskCompletion).completedStepsLaconic.forEach { step ->
            logger.info("- $step")
        }
    }

    private fun buildToolCallMessage(
        nextStepPlan: String,
        step: String,
        nextStep: NextStep,
    ): AssistantMessage = AssistantMessage(
        nextStepPlan,
        emptyMap(),
        listOf(
            AssistantMessage.ToolCall(
                step,
                "function",
                nextStep.function.tool,
                objectMapper.writeValueAsString(nextStep.function)
            )
        )
    )

    private fun buildToolResponseMessage(
        step: String,
        nextStep: NextStep,
        result: String
    ): ToolResponseMessage = ToolResponseMessage(
        listOf(
            ToolResponseMessage.ToolResponse(
                step,
                nextStep.function.tool,
                objectMapper.writeValueAsString(result)
            )
        )
    )
}
