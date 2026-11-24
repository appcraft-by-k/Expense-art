package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User-managed savings instruments. Seeded rows use fixed IDs 1..N and cannot be deleted.
 * Name uniqueness is case-insensitive and enforced in the repository.
 */
@Entity(
    tableName = "savings_types",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["sortOrder"])
    ]
)
data class SavingsTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val isSystem: Boolean,
    val isArchived: Boolean = false,
    val sortOrder: Int,
    val createdAt: Long
)
