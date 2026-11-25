package com.appcraftbyk.expense.art.domain.model

data class PaymentMethod(
    val id: Long,
    val uid: String,
    val name: String,
    val iconKey: String,
    val sortOrder: Int,
    val isSystem: Boolean,
    val isArchived: Boolean,
    val createdAt: Long
)

data class NewPaymentMethod(
    val name: String,
    val iconKey: String,
    val sortOrder: Int
)
