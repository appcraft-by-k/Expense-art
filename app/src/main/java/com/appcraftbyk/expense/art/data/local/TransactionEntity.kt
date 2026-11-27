package com.appcraftbyk.expense.art.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single money movement. Expenses and income share one table so the ledger,
 * totals, and reports can be built from a single ordered stream; [type] tells
 * the two directions apart.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["occurred_at"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: TransactionType,

    /** Amount in minor units (paise). Always positive; [type] carries the direction. */
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "note")
    val note: String? = null,

    /** Free-form label such as UPI, Cash, Bank, or Card. */
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String? = null,

    /** When the money moved, epoch milliseconds. */
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,

    /** When the row was created, epoch milliseconds. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
