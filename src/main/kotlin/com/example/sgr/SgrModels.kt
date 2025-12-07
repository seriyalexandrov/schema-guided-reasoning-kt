package com.example.sgr

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToolName(val value: String)

const val SEND_EMAIL = "send_email"
const val GET_CUSTOMER_DATA = "get_customer_data"
const val ISSUE_INVOICE = "issue_invoice"
const val VOID_INVOICE = "void_invoice"
const val REMEMBER = "remember"
const val REPORT_COMPLETION = "report_completion"

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
    JsonSubTypes.Type(value = SendEmail::class, name = SEND_EMAIL),
    JsonSubTypes.Type(value = GetCustomerData::class, name = GET_CUSTOMER_DATA),
    JsonSubTypes.Type(value = IssueInvoice::class, name = ISSUE_INVOICE),
    JsonSubTypes.Type(value = VoidInvoice::class, name = VOID_INVOICE),
    JsonSubTypes.Type(value = CreateRule::class, name = REMEMBER),
    JsonSubTypes.Type(value = ReportTaskCompletion::class, name = REPORT_COMPLETION)
)
sealed interface Function {
    @JsonIgnore
    fun toolName(): String
}

data class SendEmail(
    @field:ToolName(SEND_EMAIL)
    val tool: String = SEND_EMAIL,
    val subject: String,
    val message: String,
    val files: List<String>,
    val recipientEmail: String
) : Function {
    override fun toolName() = tool
}

data class GetCustomerData(
    @field:ToolName(GET_CUSTOMER_DATA)
    val tool: String = GET_CUSTOMER_DATA,
    val email: String
) : Function {
    override fun toolName() = tool
}

data class IssueInvoice(
    @field:ToolName(ISSUE_INVOICE)
    val tool: String = ISSUE_INVOICE,
    val email: String,
    val skus: List<String>,

    @field:JsonPropertyDescription("Between 0 and 50")
    val discountPercent: Int
) : Function {
    override fun toolName() = tool
}

data class VoidInvoice(
    @field:ToolName(VOID_INVOICE)
    val tool: String = VOID_INVOICE,
    val invoiceId: String,
    val reason: String
) : Function {
    override fun toolName() = tool
}

data class CreateRule(
    @field:ToolName(REMEMBER)
    val tool: String = REMEMBER,
    val email: String,
    val rule: String
) : Function {
    override fun toolName() = tool
}

data class ReportTaskCompletion(
    @field:ToolName(REPORT_COMPLETION)
    val tool: String = REPORT_COMPLETION,
    val completedStepsLaconic: List<String>,
    val code: String
) : Function {
    override fun toolName() = tool
}
