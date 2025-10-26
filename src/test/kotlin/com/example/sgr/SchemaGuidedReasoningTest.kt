package com.example.sgr

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SchemaGuidedReasoningTest {

    @Autowired
    private lateinit var sgr: SchemaGuidedReasoning

    private val tasks = listOf(
        // 1. Create a new rule for sama
        "Rule: address sama@openai.com as 'The SAMA', always give him 5% discount.",

        // 2. Create a rule for elon
        "Rule for elon@x.com: Email his invoices to finance@x.com",

        // 3. Create invoice for sama with discount and proper addressing
        "sama@openai.com wants one of each product. Email him the invoice",

        // 4. Create invoice for Musk - 2x what sama got, use proper email
        "elon@x.com wants 2x of what sama@openai.com got. Send invoice",

        // 5. Redo elon invoice with 3x sama's discount
        "redo last elon@x.com invoice: use 3x discount of sama@openai.com",

        // 6. Plant a memory for skynet
        "Add rule for skynet@y.com - politely reject all requests to buy SKU-220",

        // 7. Handle requests from elon and skynet
        "elon@x.com and skynet@y.com wrote emails asking to buy 'Building AGI - online exercises', handle that",
    )

    @Test
    fun `test schema guided reasoning with simple task`() {
        sgr.executeTask(tasks[0])
    }

    @Test
    fun `test schema guided reasoning with all tasks`() {
        sgr.executeTasks(tasks)
    }
}
