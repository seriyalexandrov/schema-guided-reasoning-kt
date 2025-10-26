package com.example.sgr

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
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
    private val systemPrompt = """
        You are a business assistant helping Sergei with customer interactions.

        - Clearly report when tasks are done.
        - Always send customers emails after issuing invoices (with invoice attached).
        - Be concise. Especially in emails
        - No need to wait for payment confirmation before proceeding.
        - Always check customer data before issuing invoices or making changes.

        Products: ${dbStub.products}
    """.trimIndent()

    private val promptResponseSchema = schemaBuilder.generateSchema(NextStep::class.java)

    fun executeTask(task: String): List<String> {
        println("\n=== Task: $task ===\n")

        val log = mutableListOf<Message>(
            SystemMessage(systemPrompt),
            UserMessage(task)
        )

        val completedSteps = mutableListOf<String>()

        for (i in 1..20) {
            print("Planning step_$i... ")

            val promptWithFormat = """
                ${log.last().text}

                Your response must be a JSON object following this schema:
                $promptResponseSchema

                Respond with ONLY valid JSON, no additional text.
            """.trimIndent()

            val currentLog = log.dropLast(1) + UserMessage(promptWithFormat)
            val prompt = Prompt(currentLog)

            val content = chatModel.call(prompt).result.output.text

            val nextStep = try {
                objectMapper.readValue(content, NextStep::class.java)
            } catch (e: Exception) {
                println("Failed to parse response: $content")
                throw e
            }

            // Check if a task is completed
            if (nextStep.function is ReportTaskCompletion) {
                println("agent ${nextStep.function.code}.")
                println("\n=== Summary ===")
                nextStep.function.completedStepsLaconic.forEach { step ->
                    println("- $step")
                }
                println("===============\n")
                return nextStep.function.completedStepsLaconic
            }

            // Print the next step
            val nextStepPlan = nextStep.planRemainingStepsBrief.firstOrNull() ?: "No plan"
            println(nextStepPlan)
            println("  ${nextStep.function}")

            // Execute the tool
            val result = toolsDispatcherStub.dispatch(nextStep.function)

            println("  Result: $result")

            // Add to a conversation log - include the full JSON response and tool result
            log.add(AssistantMessage(content ?: ""))
            log.add(UserMessage("Tool execution result: $result\n\nWhat's next?"))

            completedSteps.add(nextStepPlan)
        }

        println("\n!!! Max iterations reached !!!\n")
        return completedSteps
    }

    fun executeTasks(tasks: List<String>) {
        tasks.forEach { task ->
            executeTask(task)
        }
    }
}
