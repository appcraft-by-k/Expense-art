package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcraftbyk.expense.art.core.id.Uids

/**
 * System categories use fixed IDs 1..N and deterministic [uid]s so their identity survives
 * reinstall, cloud restore, and a future multi-device merge. Name uniqueness is
 * case-insensitive and enforced in the repository.
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["name", "kind"], unique = true),
        Index(value = ["kind", "sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = Uids.new(),
    val name: String,
    val kind: TransactionKind,
    val iconKey: String,
    val sortOrder: Int,
    val isSystem: Boolean,
    val isArchived: Boolean = false,
    val createdAt: Long
)
