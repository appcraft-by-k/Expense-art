package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.repository.DebtRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject

/**
 * The "settle" flow: pays whatever is still outstanding (as one repayment row) and marks the debt
 * settled atomically. Idempotent once the debt is already settled.
 */
class SettleDebt @Inject constructor(
    private val debts: DebtRepository,
    private val transactionRunner: TransactionRunner
) {

    suspend operator fun invoke(debtId: Long, settledAt: Long) = transactionRunner.inTransaction {
        val debt = debts.getDebt(debtId) ?: throw DomainException.NotFound
        if (debt.status == DebtStatus.SETTLED) return@inTransaction

        val outstanding = debts.outstandingMinor(debtId) ?: throw DomainException.NotFound
        if (outstanding > 0) {
            debts.insertRepayment(
                debtId = debtId,
                amountMinor = outstanding,
                paidAt = settledAt,
                note = null
            )
        }
        debts.markSettled(debtId, settledAt = settledAt, updatedAt = settledAt)
    }
}
