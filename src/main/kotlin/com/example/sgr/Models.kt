package com.example.sgr

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
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
    @field:JsonProperty("discount_amount") val discountAmount: Double,
    @field:JsonProperty("discount_percent") val discountPercent: Int,
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
    @field:JsonProperty(required = true) override val tool: String = "send_email",
    @field:JsonProperty(required = true) val subject: String,
    @field:JsonProperty(required = true) val message: String,
    @field:JsonProperty(required = true) val files: List<String>,
    @field:JsonProperty("recipient_email", required = true) val recipientEmail: String
) : ToolCommand

data class GetCustomerData(
    @field:JsonProperty(required = true) override val tool: String = "get_customer_data",
    @field:JsonProperty(required = true) val email: String
) : ToolCommand

data class IssueInvoice(
    @field:JsonProperty(required = true) override val tool: String = "issue_invoice",
    @param:JsonProperty(required = true) val email: String,
    @field:JsonProperty(required = true) val skus: List<String>,
    @field:JsonProperty("discount_percent", required = true) val discountPercent: Int
) : ToolCommand

data class VoidInvoice(
    @field:JsonProperty(required = true) override val tool: String = "void_invoice",
    @field:JsonProperty("invoice_id", required = true) val invoiceId: String,
    @field:JsonProperty(required = true) val reason: String
) : ToolCommand

data class CreateRule(
    @field:JsonProperty(required = true) override val tool: String = "remember",
    @field:JsonProperty(required = true) val email: String,
    @field:JsonProperty(required = true) val rule: String
) : ToolCommand

data class ReportTaskCompletion(
    @field:JsonProperty(required = true) override val tool: String = "report_completion",
    @field:JsonProperty("completed_steps_laconic", required = true) val completedStepsLaconic: List<String>,
    @field:JsonProperty(required = true) val code: String
) : ToolCommand

// NextStep schema for reasoning
data class NextStep(
    @field:JsonProperty(required = true) val currentState: String,
    @field:JsonProperty(required = true) val planRemainingStepsBrief: List<String>,
    @field:JsonProperty(required = true)
    @field:JsonPropertyDescription("Set true if the task has been completed, false otherwise")
    val taskCompleted: Boolean,
    @field:JsonProperty(required = true) val function: ToolCommand
)
