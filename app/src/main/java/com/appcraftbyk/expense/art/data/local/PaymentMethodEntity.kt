package com.appcraftbyk.expense.art.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * How money moved (cash, card, UPI, ...). Default rows are seeded on first
 * launch; users can add their own. Names are unique case-insensitively.
 */
@Entity(
    tableName = "payment_methods",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE)
    val name: String,

    /** True for the built-in catalog rows created by the app. */
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
