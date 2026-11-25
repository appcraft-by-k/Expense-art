package com.appcraftbyk.expense.art.domain.repository

import com.appcraftbyk.expense.art.domain.model.Debt
import com.appcraftbyk.expense.art.domain.model.DebtContact
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtListFilter
import com.appcraftbyk.expense.art.domain.model.DebtRepayment
import com.appcraftbyk.expense.art.domain.model.DebtWithContact
import com.appcraftbyk.expense.art.domain.model.UpcomingDueItem
import kotlinx.coroutines.flow.Flow

interface DebtRepository {

    /** Outstanding per direction, derived in SQL and never netted. */
    fun observeOutstandingByDirection(): Flow<Map<DebtDirection, Long>>

    fun observeUpcomingDue(fromEpochDay: Long, toEpochDay: Long): Flow<List<UpcomingDueItem>>

    fun observeDebts(filter: DebtListFilter, todayEpochDay: Long): Flow<List<DebtWithContact>>

    fun observeDebtWithContact(debtId: Long): Flow<DebtWithContact?>

    suspend fun getDebt(debtId: Long): Debt?

    /** `principalMinor - SUM(repayments)`; null when the debt is gone. */
    suspend fun outstandingMinor(debtId: Long): Long?

    fun observeRepayments(debtId: Long): Flow<List<DebtRepayment>>

    suspend fun insertRepayment(debtId: Long, amountMinor: Long, paidAt: Long, note: String?): Long

    suspend fun markSettled(debtId: Long, settledAt: Long, updatedAt: Long)

    fun observeContacts(): Flow<List<DebtContact>>
}
