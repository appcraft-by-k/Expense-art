package com.appcraftbyk.expense.art.domain.model

/** Ledger row with its joined category and payment-method labels for display. */
data class Transaction(
    val id: Long,
    val uid: String,
    val kind: TransactionKind,
    val amountMinor: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val occurredAt: Long,
    val paymentMethodId: Long,
    val paymentMethodName: String,
    val note: String?
)

/** Input for [com.appcraftbyk.expense.art.domain.usecase.AddTransaction]. */
data class NewTransaction(
    val kind: TransactionKind,
    val amountMinor: Long,
    val categoryId: Long?,
    val paymentMethodId: Long,
    val occurredAt: Long? = null,
    val note: String? = null
)

data class CategorySpendItem(
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val totalMinor: Long
)
