package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.repository.PaymentMethodRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject

/**
 * Deletes a user payment method. Used methods must be reassigned inside the same transaction;
 * seeded methods can only be archived.
 */
class DeletePaymentMethod @Inject constructor(
    private val paymentMethods: PaymentMethodRepository,
    private val transactionRunner: TransactionRunner,
    private val appClock: AppClock
) {

    suspend operator fun invoke(paymentMethodId: Long, reassignToId: Long? = null) =
        transactionRunner.inTransaction {
            val method = paymentMethods.get(paymentMethodId) ?: throw DomainException.NotFound
            if (method.isSystem) throw DomainException.SystemRowNotDeletable

            val usage = paymentMethods.countUsage(paymentMethodId)
            if (usage > 0) {
                val target = reassignToId?.let {
                    paymentMethods.get(it) ?: throw DomainException.NotFound
                } ?: throw DomainException.ReassignmentRequired(usage)

                if (target.id == method.id || target.isArchived) {
                    throw DomainException.InvalidReassignment
                }
                paymentMethods.reassignTransactions(
                    fromPaymentMethodId = paymentMethodId,
                    toPaymentMethodId = target.id,
                    updatedAt = appClock.nowMillis()
                )
            }
            paymentMethods.delete(paymentMethodId)
        }
}
