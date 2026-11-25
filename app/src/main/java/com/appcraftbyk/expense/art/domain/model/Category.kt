package com.appcraftbyk.expense.art.domain.model

/** [uid] is immutable once created; repositories never regenerate it on edit. */
data class Category(
    val id: Long,
    val uid: String,
    val name: String,
    val kind: TransactionKind,
    val iconKey: String,
    val sortOrder: Int,
    val isSystem: Boolean,
    val isArchived: Boolean,
    val createdAt: Long
)

data class NewCategory(
    val name: String,
    val kind: TransactionKind,
    val iconKey: String,
    val sortOrder: Int
)
