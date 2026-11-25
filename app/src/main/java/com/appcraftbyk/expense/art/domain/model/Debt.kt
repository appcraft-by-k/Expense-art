package com.appcraftbyk.expense.art.domain.model

data class Debt(
    val id: Long,
    val uid: String,
    val contactId: Long,
    val direction: DebtDirection,
    val principalMinor: Long,
    val startDate: Long,
    val dueDate: Long?,
    val note: String?,
    val status: DebtStatus,
    val settledAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

/** Debt joined with its contact and derived repaid total; outstanding is never stored. */
data class DebtWithContact(
    val id: Long,
    val contactId: Long,
    val contactName: String,
    val direction: DebtDirection,
    val principalMinor: Long,
    val repaidMinor: Long,
    val startDate: Long,
    val dueDate: Long?,
    val note: String?,
    val status: DebtStatus,
    val settledAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val outstandingMinor: Long get() = (principalMinor - repaidMinor).coerceAtLeast(0L)
}

data class DebtRepayment(
    val id: Long,
    val uid: String,
    val debtId: Long,
    val amountMinor: Long,
    val paidAt: Long,
    val note: String?
)

data class DebtContact(
    val id: Long,
    val uid: String,
    val name: String,
    val note: String?,
    val isArchived: Boolean,
    val createdAt: Long
)

data class UpcomingDueItem(
    val debtId: Long,
    val contactName: String,
    val direction: DebtDirection,
    val dueMinor: Long,
    /** Epoch day. */
    val dueDate: Long
)
