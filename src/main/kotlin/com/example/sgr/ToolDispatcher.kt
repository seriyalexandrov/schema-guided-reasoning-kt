package com.example.sgr

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class ToolDispatcher(
    private val db: Database,
    private val objectMapper: ObjectMapper
) {
    fun dispatch(cmd: ToolCommand): String {
        return when (cmd) {
            is SendEmail -> {
                val email = Email(
                    to = cmd.recipientEmail,
                    subject = cmd.subject,
                    message = cmd.message
                )
                db.emails.add(email)
                objectMapper.writeValueAsString(email)
            }

            is CreateRule -> {
                val rule = Rule(
                    email = cmd.email,
                    rule = cmd.rule
                )
                db.rules.add(rule)
                objectMapper.writeValueAsString(rule)
            }

            is GetCustomerData -> {
                val addr = cmd.email
                val customerData = CustomerData(
                    rules = db.rules.filter { it.email == addr },
                    invoices = db.invoices.entries
                        .filter { it.value.email == addr }
                        .map { it.key to it.value },
                    emails = db.emails.filter { it.to == addr }
                )
                objectMapper.writeValueAsString(customerData)
            }

            is IssueInvoice -> {
                var total = 0.0
                for (sku in cmd.skus) {
                    val product = db.products[sku]
                    if (product == null) {
                        return "Product $sku not found"
                    }
                    total += product.price
                }

                val discount = ((total * cmd.discountPercent / 100.0) * 100).roundToInt() / 100.0

                val invoiceId = "INV-${db.invoices.size + 1}"

                val invoice = Invoice(
                    id = invoiceId,
                    email = cmd.email,
                    file = "/invoices/$invoiceId.pdf",
                    skus = cmd.skus,
                    discountAmount = discount,
                    discountPercent = cmd.discountPercent,
                    total = total,
                    void = false
                )
                db.invoices[invoiceId] = invoice
                objectMapper.writeValueAsString(invoice)
            }

            is VoidInvoice -> {
                val invoice = db.invoices[cmd.invoiceId]
                if (invoice == null) {
                    return "Invoice ${cmd.invoiceId} not found"
                }
                invoice.void = true
                objectMapper.writeValueAsString(invoice)
            }

            is ReportTaskCompletion -> {
                objectMapper.writeValueAsString(cmd)
            }
        }
    }
}
