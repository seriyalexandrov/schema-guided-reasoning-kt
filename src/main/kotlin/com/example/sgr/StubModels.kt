package com.example.sgr

data class Product(
    val name: String,
    val price: Double
)

data class Email(
    val to: String,
    val subject: String,
    val message: String
)

data class Rule(
    val email: String,
    val rule: String
)

data class Invoice(
    val id: String,
    val email: String,
    val file: String,
    val skus: List<String>,
    val discountAmount: Double,
    val discountPercent: Int,
    val total: Double,
    var void: Boolean = false
)

data class CustomerData(
    val rules: List<Rule>,
    val invoices: List<Pair<String, Invoice>>,
    val emails: List<Email>
)
