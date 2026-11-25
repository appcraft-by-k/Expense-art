package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject

/**
 * Adds money to or removes money from a pot. Withdrawals may not take the balance below zero;
 * the balance check and the insert share one transaction.
 */
class ContributeToPot @Inject constructor(
    private val savings: SavingsRepository,
    private val transactionRunner: TransactionRunner,
    private val appClock: AppClock
) {

    suspend operator fun invoke(
        goalId: Long,
        kind: SavingsEntryKind,
        amountMinor: Long,
        occurredAt: Long? = null,
        note: String? = null
    ) = transactionRunner.inTransaction {
        if (amountMinor <= 0) throw DomainException.InvalidAmount

        val pot = savings.getPot(goalId) ?: throw DomainException.NotFound
        if (pot.isArchived) throw DomainException.ArchivedTarget

        if (kind == SavingsEntryKind.WITHDRAWAL) {
            val balance = savings.potBalanceMinor(goalId)
            if (amountMinor > balance) throw DomainException.WithdrawalExceedsBalance(balance)
        }

        savings.insertEntry(
            goalId = goalId,
            kind = kind,
            amountMinor = amountMinor,
            occurredAt = occurredAt ?: appClock.nowMillis(),
            note = note?.trim()?.takeIf { it.isNotEmpty() }
        )
        Unit
    }
}
