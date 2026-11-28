package com.appcraftbyk.expense.art.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
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
        Index(value = ["type"]),
        Index(value = ["category_id"]),
        Index(value = ["payment_method_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["payment_method_id"],
            onDelete = ForeignKey.SET_NULL
        )
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

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "note")
    val note: String? = null,

    /** Null when the user has not recorded how the money moved. */
    @ColumnInfo(name = "payment_method_id")
    val paymentMethodId: Long? = null,

    /** When the money moved, epoch milliseconds. */
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,

    /** When the row was created, epoch milliseconds. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
