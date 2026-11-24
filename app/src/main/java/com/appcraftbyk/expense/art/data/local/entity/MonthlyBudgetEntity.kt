package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcraftbyk.expense.art.core.id.Uids

/** One budget per calendar month; [yearMonth] is packed as `202611`. */
@Entity(
    tableName = "monthly_budgets",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["yearMonth"], unique = true)
    ]
)
data class MonthlyBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = Uids.new(),
    val yearMonth: Int,
    val limitMinor: Long,
    val createdAt: Long
)
