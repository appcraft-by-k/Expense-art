package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A category may only be deleted when unused; FK RESTRICT protects history. */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["occurredAt"]),
        Index(value = ["categoryId"]),
        Index(value = ["kind", "occurredAt"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: TransactionKind,
    /** Positive minor units (paise); the sign comes from [kind]. */
    val amountMinor: Long,
    val categoryId: Long?,
    /** Epoch millis. */
    val occurredAt: Long,
    val paymentMethod: PaymentMethod,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)
