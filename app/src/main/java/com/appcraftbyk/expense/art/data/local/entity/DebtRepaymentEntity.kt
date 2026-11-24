package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Deleting a debt removes its repayment history; debts are never hard-deleted while open. */
@Entity(
    tableName = "debt_repayments",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["debtId", "paidAt"]),
        Index(value = ["paidAt"])
    ]
)
data class DebtRepaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    /** Positive minor units (paise). */
    val amountMinor: Long,
    /** Epoch millis. */
    val paidAt: Long,
    val note: String?
)
