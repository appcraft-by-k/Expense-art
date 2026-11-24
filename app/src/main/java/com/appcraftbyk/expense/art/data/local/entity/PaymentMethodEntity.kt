package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcraftbyk.expense.art.core.id.Uids

/**
 * Payment methods are user-managed, mirroring categories: seeded rows are non-deletable,
 * user rows may be deleted only when unused. Name uniqueness is case-insensitive and
 * enforced in the repository.
 */
@Entity(
    tableName = "payment_methods",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["name"], unique = true),
        Index(value = ["sortOrder"])
    ]
)
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = Uids.new(),
    val name: String,
    val iconKey: String,
    val isSystem: Boolean,
    val isArchived: Boolean = false,
    val sortOrder: Int,
    val createdAt: Long
)
