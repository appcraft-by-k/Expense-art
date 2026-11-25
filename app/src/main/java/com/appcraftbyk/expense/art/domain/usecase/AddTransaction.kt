package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.NewTransaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.repository.CategoryRepository
import com.appcraftbyk.expense.art.domain.repository.PaymentMethodRepository
import com.appcraftbyk.expense.art.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Validates a manual entry, then writes it. Categories and payment methods must exist and be
 * active; a transaction can never reference an archived picker row created after the fact.
 */
class AddTransaction @Inject constructor(
    private val transactions: TransactionRepository,
    private val categories: CategoryRepository,
    private val paymentMethods: PaymentMethodRepository,
    private val appClock: AppClock
) {

    suspend operator fun invoke(input: NewTransaction): Long {
        if (input.amountMinor <= 0) throw DomainException.InvalidAmount

        if (input.categoryId != null) {
            val category = categories.get(input.categoryId) ?: throw DomainException.NotFound
            if (category.kind != input.kind) throw DomainException.CategoryKindMismatch
            if (category.isArchived) throw DomainException.ArchivedTarget
        }
        val paymentMethod = paymentMethods.get(input.paymentMethodId)
            ?: throw DomainException.NotFound
        if (paymentMethod.isArchived) throw DomainException.ArchivedTarget

        return transactions.add(
            kind = input.kind,
            amountMinor = input.amountMinor,
            categoryId = input.categoryId,
            occurredAt = input.occurredAt ?: appClock.nowMillis(),
            paymentMethodId = input.paymentMethodId,
            note = input.note?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    /** Convenience for the common "expense with defaults" path. */
    suspend fun expense(amountMinor: Long, categoryId: Long?, paymentMethodId: Long): Long =
        invoke(
            NewTransaction(
                kind = TransactionKind.EXPENSE,
                amountMinor = amountMinor,
                categoryId = categoryId,
                paymentMethodId = paymentMethodId
            )
        )
}
