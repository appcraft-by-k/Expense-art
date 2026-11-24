package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcraftbyk.expense.art.core.id.Uids

/**
 * A savings pot ("HDFC FD 2026", "Emergency Fund"). Target and deadline are optional because
 * instrument pots often have neither. [deadline] is an epoch day.
 */
@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = SavingsTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["typeId"]),
        Index(value = ["isArchived", "sortOrder"])
    ]
)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = Uids.new(),
    val name: String,
    val typeId: Long,
    val institution: String?,
    val targetMinor: Long?,
    val deadline: Long?,
    val isArchived: Boolean = false,
    val sortOrder: Int,
    val createdAt: Long
)
