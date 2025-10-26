package com.example.sgr

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SchemaGuidedReasoningDemo {

    @Autowired
    private lateinit var sgr: SchemaGuidedReasoning

    private val tasks = listOf(
        // 1. this one should create a new rule for sama
        "Rule: address sama@openai.com as 'The SAMA', always give him 5% discount.",
        // 2. this should create a rule for elon
        "Rule for elon@x.com: Email his invoices to finance@x.com",
        // 3. now, this task should create an invoice for sama that includes one of each
        // product. But it should also remember to give discount and address him
        // properly
        "sama@openai.com wants one of each product. Email him the invoice",
        // 4. Even more tricky - we need to create the invoice for Musk based on the
        // invoice of sama, but twice. Plus LLM needs to remeber to use the proper
        // email address for invoices - finance@x.com
        "elon@x.com wants 2x of what sama@openai.com got. Send invoice",
        // 5. even more tricky. Need to cancel old invoice (we never told LLMs how)
        // and issue the new invoice. BUT it should pull the discount from sama and
        // triple it. Obviously the model should also remember to send invoice
        // not to elon@x.com but to finance@x.com
        "redo last elon@x.com invoice: use 3x discount of sama@openai.com",
        // let's demonstrate how the agent can change its plans after discovering new information
        // first we plant a new memory
        "Add rule for skynet@y.com - politely reject all requests to buy SKU-220",
        // now let's give another task (agent will not have the memory above in the context UNTIL
        // it is pulled from memory store)
        "elon@x.com and skynet@y.com wrote emails asking to buy 'Building AGI - online exercises', handle that",
    )

    @Test
    fun `test schema guided reasoning with all tasks`() {
        sgr.executeTasks(tasks)
    }
}
