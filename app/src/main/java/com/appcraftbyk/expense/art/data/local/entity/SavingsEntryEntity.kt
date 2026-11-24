package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Pot balance is derived: SUM(contributions) - SUM(withdrawals). Never stored. */
@Entity(
    tableName = "savings_entries",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId", "occurredAt"])]
)
data class SavingsEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val kind: SavingsEntryKind,
    /** Positive minor units (paise). */
    val amountMinor: Long,
    /** Epoch millis. */
    val occurredAt: Long,
    val note: String?
)
