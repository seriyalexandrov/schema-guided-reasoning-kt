package com.example.sgr

import org.springframework.stereotype.Component

@Component
class DatabaseStub {
    val rules = mutableListOf<Rule>()
    val invoices = mutableMapOf<String, Invoice>()
    val emails = mutableListOf<Email>()
    val products = mapOf(
        "SKU-205" to Product("AGI 101 Course Personal", 258.0),
        "SKU-210" to Product("AGI 101 Course Team (5 seats)", 1290.0),
        "SKU-220" to Product("Building AGI - online exercises", 315.0)
    )
}
