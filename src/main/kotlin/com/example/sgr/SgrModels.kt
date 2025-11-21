package com.example.sgr

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class NextStep(

    @field:JsonProperty("current_state", required = true)
    val currentState: String,

    @field:JsonProperty("plan_remaining_steps_brief", required = true)
    @field:JsonPropertyDescription("Min 1 step and max 5 steps")
    val planRemainingStepsBrief: List<String>,

    @field:JsonProperty("task_completed", required = true)
    val taskCompleted: Boolean,

    @field:JsonPropertyDescription("execute first remaining step")
    @field:JsonProperty(required = true)
    val function: Function
) {
    @JsonIgnore
    fun isTaskCompleted(): Boolean = this.function is ReportTaskCompletion
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "tool",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SendEmail::class, name = "send_email"),
    JsonSubTypes.Type(value = GetCustomerData::class, name = "get_customer_data"),
    JsonSubTypes.Type(value = IssueInvoice::class, name = "issue_invoice"),
    JsonSubTypes.Type(value = VoidInvoice::class, name = "void_invoice"),
    JsonSubTypes.Type(value = CreateRule::class, name = "remember"),
    JsonSubTypes.Type(value = ReportTaskCompletion::class, name = "report_completion")
)
sealed interface Function {
    @get:JsonProperty(required = true)
    @get:JsonPropertyDescription("Tool name identifier")
    val tool: String
}

data class SendEmail(
    @field:JsonProperty(required = true, defaultValue = "send_email")
    override val tool: String = "send_email",
    @field:JsonProperty(required = true)
    val subject: String,
    @field:JsonProperty(required = true)
    val message: String,
    @field:JsonProperty(required = true)
    val files: List<String>,
    @field:JsonProperty("recipient_email", required = true)
    val recipientEmail: String
) : Function

data class GetCustomerData(
    @field:JsonProperty(required = true, defaultValue = "get_customer_data")
    override val tool: String = "get_customer_data",
    @field:JsonProperty(required = true)
    val email: String
) : Function

data class IssueInvoice(
    @field:JsonProperty(required = true, defaultValue = "issue_invoice")
    override val tool: String = "issue_invoice",
    @field:JsonProperty(required = true)
    val email: String,
    @field:JsonProperty(required = true)
    val skus: List<String>,

    @field:JsonProperty("discount_percent", required = true)
    @field:JsonPropertyDescription("Between 0 and 50")
    val discountPercent: Int
) : Function

data class VoidInvoice(
    @field:JsonProperty(required = true, defaultValue = "void_invoice")
    override val tool: String = "void_invoice",
    @field:JsonProperty("invoice_id", required = true)
    val invoiceId: String,
    @field:JsonProperty(required = true)
    val reason: String
) : Function

data class CreateRule(
    @field:JsonProperty(required = true, defaultValue = "remember")
    override val tool: String = "remember",
    @field:JsonProperty(required = true)
    val email: String,
    @field:JsonProperty(required = true)
    val rule: String
) : Function

data class ReportTaskCompletion(
    @field:JsonProperty(required = true, defaultValue = "report_completion")
    override val tool: String = "report_completion",
    @field:JsonProperty("completed_steps_laconic", required = true)
    val completedStepsLaconic: List<String>,
    @field:JsonProperty(required = true)
    val code: String
) : Function
