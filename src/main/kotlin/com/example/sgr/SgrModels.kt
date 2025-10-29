package com.example.sgr

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class NextStep(

    val currentState: String,

    @field:JsonPropertyDescription("Min 1 step and max 5 steps")
    val planRemainingStepsBrief: List<String>,

    val taskCompleted: Boolean,

    @field:JsonPropertyDescription("Execute first remaining step")
    val function: Function
) {
    @JsonIgnore
    fun isTaskCompleted(): Boolean = this.function is ReportTaskCompletion
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tool")
@JsonSubTypes(
    JsonSubTypes.Type(value = SendEmail::class, name = "send_email"),
    JsonSubTypes.Type(value = GetCustomerData::class, name = "get_customer_data"),
    JsonSubTypes.Type(value = IssueInvoice::class, name = "issue_invoice"),
    JsonSubTypes.Type(value = VoidInvoice::class, name = "void_invoice"),
    JsonSubTypes.Type(value = CreateRule::class, name = "remember"),
    JsonSubTypes.Type(value = ReportTaskCompletion::class, name = "report_completion")
)
sealed interface Function {
    @get:JsonIgnore
    val tool: String
}

data class SendEmail(
    override val tool: String = "send_email",
    val subject: String,
    val message: String,
    val files: List<String>,
    val recipientEmail: String
) : Function

data class GetCustomerData(
    override val tool: String = "get_customer_data",
    val email: String
) : Function

data class IssueInvoice(
    override val tool: String = "issue_invoice",
    val email: String,
    val skus: List<String>,

    @field:JsonPropertyDescription("Between 0 and 50")
    val discountPercent: Int
) : Function

data class VoidInvoice(
    override val tool: String = "void_invoice",
    val invoiceId: String,
    val reason: String
) : Function

data class CreateRule(
    override val tool: String = "remember",
    val email: String,
    val rule: String
) : Function

data class ReportTaskCompletion(
    override val tool: String = "report_completion",
    val completedStepsLaconic: List<String>,
    val code: String
) : Function
