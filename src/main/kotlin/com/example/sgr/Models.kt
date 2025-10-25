package com.example.sgr

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// Product model
data class Product(
    val name: String,
    val price: Double
)

// Email log entry
data class Email(
    val to: String,
    val subject: String,
    val message: String
)

// Customer rule
data class Rule(
    val email: String,
    val rule: String
)

// Invoice model
data class Invoice(
    val id: String,
    val email: String,
    val file: String,
    val skus: List<String>,
    @JsonProperty("discount_amount") val discountAmount: Double,
    @JsonProperty("discount_percent") val discountPercent: Int,
    val total: Double,
    var void: Boolean = false
)

// Customer data response
data class CustomerData(
    val rules: List<Rule>,
    val invoices: List<Pair<String, Invoice>>,
    val emails: List<Email>
)

// Tool commands
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tool")
@JsonSubTypes(
    JsonSubTypes.Type(value = SendEmail::class, name = "send_email"),
    JsonSubTypes.Type(value = GetCustomerData::class, name = "get_customer_data"),
    JsonSubTypes.Type(value = IssueInvoice::class, name = "issue_invoice"),
    JsonSubTypes.Type(value = VoidInvoice::class, name = "void_invoice"),
    JsonSubTypes.Type(value = CreateRule::class, name = "remember"),
    JsonSubTypes.Type(value = ReportTaskCompletion::class, name = "report_completion")
)
sealed interface ToolCommand {
    val tool: String
}

data class SendEmail(
    override val tool: String = "send_email",
    val subject: String,
    val message: String,
    val files: List<String>,
    @JsonProperty("recipient_email") val recipientEmail: String
) : ToolCommand

data class GetCustomerData(
    override val tool: String = "get_customer_data",
    val email: String
) : ToolCommand

data class IssueInvoice(
    override val tool: String = "issue_invoice",
    val email: String,
    val skus: List<String>,
    @JsonProperty("discount_percent") val discountPercent: Int
) : ToolCommand

data class VoidInvoice(
    override val tool: String = "void_invoice",
    @JsonProperty("invoice_id") val invoiceId: String,
    val reason: String
) : ToolCommand

data class CreateRule(
    override val tool: String = "remember",
    val email: String,
    val rule: String
) : ToolCommand

data class ReportTaskCompletion(
    override val tool: String = "report_completion",
    @JsonProperty("completed_steps_laconic") val completedStepsLaconic: List<String>,
    val code: String
) : ToolCommand

// NextStep schema for reasoning
data class NextStep(
    @JsonProperty("current_state") val currentState: String = "",
    @JsonProperty("plan_remaining_steps_brief") val planRemainingStepsBrief: List<String> = emptyList(),
    @JsonProperty("task_completed") val taskCompleted: Boolean = false,
    val function: ToolCommand
)
