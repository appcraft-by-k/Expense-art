package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.repository.DebtRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject

/**
 * Records a repayment and settles the debt in the same transaction when the outstanding balance
 * reaches zero. Never writes a ledger transaction (debt flow must not distort budgets).
 */
class RecordRepayment @Inject constructor(
    private val debts: DebtRepository,
    private val transactionRunner: TransactionRunner
) {

    suspend operator fun invoke(
        debtId: Long,
        amountMinor: Long,
        paidAt: Long,
        note: String? = null
    ) = transactionRunner.inTransaction {
        if (amountMinor <= 0) throw DomainException.InvalidAmount

        val debt = debts.getDebt(debtId) ?: throw DomainException.NotFound
        if (debt.status == DebtStatus.SETTLED) throw DomainException.AlreadySettled

        val outstanding = debts.outstandingMinor(debtId) ?: throw DomainException.NotFound
        if (amountMinor > outstanding) throw DomainException.OverPayment(outstanding)

        debts.insertRepayment(
            debtId = debtId,
            amountMinor = amountMinor,
            paidAt = paidAt,
            note = note?.trim()?.takeIf { it.isNotEmpty() }
        )

        if (amountMinor == outstanding) {
            debts.markSettled(debtId, settledAt = paidAt, updatedAt = paidAt)
        }
    }
}
