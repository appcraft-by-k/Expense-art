package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * System categories use fixed IDs 1..N so their identity survives reinstall/restore.
 * Name uniqueness is case-insensitive and enforced in the repository.
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "kind"], unique = true),
        Index(value = ["kind", "sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: TransactionKind,
    val iconKey: String,
    val sortOrder: Int,
    val isSystem: Boolean,
    val isArchived: Boolean = false,
    val createdAt: Long
)
