package com.appcraftbyk.expense.art.domain.model

data class SavingsType(
    val id: Long,
    val uid: String,
    val name: String,
    val iconKey: String,
    val isSystem: Boolean,
    val isArchived: Boolean,
    val sortOrder: Int,
    val createdAt: Long
)

data class NewSavingsType(
    val name: String,
    val iconKey: String,
    val sortOrder: Int
)

/** A savings pot with its derived balance (contributions minus withdrawals). */
data class SavingsPot(
    val id: Long,
    val uid: String,
    val name: String,
    val typeId: Long,
    val typeName: String,
    val institution: String?,
    val targetMinor: Long?,
    /** Epoch day. */
    val deadline: Long?,
    val balanceMinor: Long,
    val isArchived: Boolean,
    val sortOrder: Int,
    val createdAt: Long
) {
    /** Capped at 100 so the progress bar never overflows. */
    val progressPercent: Int?
        get() = targetMinor?.takeIf { it > 0 }
            ?.let { (balanceMinor.coerceAtLeast(0) * 100 / it).coerceAtMost(100).toInt() }
}

data class NewSavingsPot(
    val name: String,
    val typeId: Long,
    val institution: String?,
    val targetMinor: Long?,
    val deadline: Long?,
    val sortOrder: Int
)

data class SavingsEntry(
    val id: Long,
    val uid: String,
    val goalId: Long,
    val kind: SavingsEntryKind,
    val amountMinor: Long,
    val occurredAt: Long,
    val note: String?
)
