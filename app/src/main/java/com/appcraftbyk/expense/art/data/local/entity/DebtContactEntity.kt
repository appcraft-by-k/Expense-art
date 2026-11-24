package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcraftbyk.expense.art.core.id.Uids

@Entity(
    tableName = "debt_contacts",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["name"])
    ]
)
data class DebtContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = Uids.new(),
    val name: String,
    val note: String?,
    val isArchived: Boolean = false,
    val createdAt: Long
)
