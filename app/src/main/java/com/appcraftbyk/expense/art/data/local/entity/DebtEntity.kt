package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Outstanding balance is always derived (`principalMinor - SUM(repayments)`); it is never stored.
 * Calendar-only dates use epoch days.
 */
@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = DebtContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["direction", "status"]),
        Index(value = ["contactId"]),
        Index(value = ["dueDate"])
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val direction: DebtDirection,
    val principalMinor: Long,
    /** Epoch day. */
    val startDate: Long,
    /** Epoch day; drives the Overdue filter. */
    val dueDate: Long?,
    val note: String?,
    val status: DebtStatus = DebtStatus.OPEN,
    val settledAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
