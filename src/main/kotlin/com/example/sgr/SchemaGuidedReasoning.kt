package com.example.sgr

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.stereotype.Service

@Service
class SchemaGuidedReasoning(
    private val db: Database,
    private val dispatcher: ToolDispatcher,
    private val objectMapper: ObjectMapper,
    private val chatModel: OpenAiChatModel
) {
    private val systemPrompt = """
        You are a business assistant helping Rinat Abdullin with customer interactions.

        - Clearly report when tasks are done.
        - Always send customers emails after issuing invoices (with invoice attached).
        - Be laconic. Especially in emails
        - No need to wait for payment confirmation before proceeding.
        - Always check customer data before issuing invoices or making changes.

        Products: ${db.products}
    """.trimIndent()

    private val jsonSchema = """
{
  "type": "object",
  "required": ["current_state", "plan_remaining_steps_brief", "task_completed", "function"],
  "properties": {
    "current_state": {
      "type": "string",
      "description": "Brief summary of current progress"
    },
    "plan_remaining_steps_brief": {
      "type": "array",
      "description": "Remaining steps to complete, in order",
      "items": {"type": "string"}
    },
    "task_completed": {
      "type": "boolean",
      "description": "Whether all tasks are fully completed"
    },
    "function": {
      "oneOf": [
        {
          "type": "object",
          "required": ["tool", "email"],
          "properties": {
            "tool": {"type": "string", "const": "get_customer_data"},
            "email": {"type": "string", "description": "Customer email address"}
          }
        },
        {
          "type": "object",
          "required": ["tool", "email", "rule"],
          "properties": {
            "tool": {"type": "string", "const": "remember"},
            "email": {"type": "string", "description": "Customer email address"},
            "rule": {"type": "string", "description": "Rule to remember"}
          }
        },
        {
          "type": "object",
          "required": ["tool", "email", "skus", "discount_percent"],
          "properties": {
            "tool": {"type": "string", "const": "issue_invoice"},
            "email": {"type": "string", "description": "Customer email address"},
            "skus": {"type": "array", "items": {"type": "string"}, "description": "Product SKUs to invoice"},
            "discount_percent": {"type": "integer", "description": "Discount percentage (0-100)"}
          }
        },
        {
          "type": "object",
          "required": ["tool", "invoice_id", "reason"],
          "properties": {
            "tool": {"type": "string", "const": "void_invoice"},
            "invoice_id": {"type": "string", "description": "Invoice ID to void"},
            "reason": {"type": "string", "description": "Reason for voiding"}
          }
        },
        {
          "type": "object",
          "required": ["tool", "subject", "message", "files", "recipient_email"],
          "properties": {
            "tool": {"type": "string", "const": "send_email"},
            "subject": {"type": "string", "description": "Email subject"},
            "message": {"type": "string", "description": "Email body"},
            "files": {"type": "array", "items": {"type": "string"}, "description": "File paths to attach"},
            "recipient_email": {"type": "string", "description": "Recipient email address"}
          }
        },
        {
          "type": "object",
          "required": ["tool", "completed_steps_laconic", "code"],
          "properties": {
            "tool": {"type": "string", "const": "report_completion"},
            "completed_steps_laconic": {"type": "array", "items": {"type": "string"}, "description": "Summary of completed steps"},
            "code": {"type": "string", "description": "Status code (OK or ERROR)"}
          }
        }
      ]
    }
  }
}
""".trimIndent()

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
                ${log.last().content}

                Your response must be a JSON object following this schema:
                $jsonSchema

                Respond with ONLY valid JSON, no additional text.
            """.trimIndent()

            val currentLog = log.dropLast(1) + UserMessage(promptWithFormat)
            val prompt = Prompt(currentLog)

            val response = chatModel.call(prompt)
            val content = response.result.output.content

            val nextStep = try {
                objectMapper.readValue(content, NextStep::class.java)
            } catch (e: Exception) {
                println("Failed to parse response: $content")
                throw e
            }

            // Check if task is completed
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
            println("$nextStepPlan")
            println("  ${nextStep.function}")

            // Execute the tool
            val result = dispatcher.dispatch(nextStep.function)

            println("  Result: $result")

            // Add to conversation log - include the full JSON response and tool result
            log.add(AssistantMessage(content))
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
