package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debt_contacts",
    indices = [Index(value = ["name"])]
)
data class DebtContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String?,
    val isArchived: Boolean = false,
    val createdAt: Long
)
