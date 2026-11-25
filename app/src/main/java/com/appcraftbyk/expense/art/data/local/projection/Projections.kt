package com.appcraftbyk.expense.art.data.local.projection

import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.model.TransactionKind

/** Aggregate result POJOs; every figure is computed in SQL, never in memory. */

data class KindTotal(
    val kind: TransactionKind,
    val totalMinor: Long
)

data class CategorySpend(
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val totalMinor: Long
)

data class TransactionWithCategory(
    val id: Long,
    val uid: String,
    val kind: TransactionKind,
    val amountMinor: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val occurredAt: Long,
    val paymentMethodId: Long,
    val paymentMethodName: String,
    val note: String?
)

data class DirectionOutstanding(
    val direction: DebtDirection,
    val outstandingMinor: Long
)

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

data class UpcomingDue(
    val debtId: Long,
    val contactName: String,
    val direction: DebtDirection,
    val dueMinor: Long,
    val dueDate: Long
)

data class SavingsPotWithBalance(
    val id: Long,
    val uid: String,
    val name: String,
    val typeId: Long,
    val typeName: String,
    val institution: String?,
    val targetMinor: Long?,
    val deadline: Long?,
    val balanceMinor: Long,
    val isArchived: Boolean,
    val sortOrder: Int,
    val createdAt: Long
)
