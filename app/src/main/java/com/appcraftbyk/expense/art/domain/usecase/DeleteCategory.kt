package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.repository.CategoryRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject

/**
 * Deletes a user category. If transactions still reference it, a target category of the same
 * kind must be supplied and the reassign + delete runs in one transaction, preserving history.
 */
class DeleteCategory @Inject constructor(
    private val categories: CategoryRepository,
    private val transactionRunner: TransactionRunner,
    private val appClock: AppClock
) {

    suspend operator fun invoke(categoryId: Long, reassignToId: Long? = null) =
        transactionRunner.inTransaction {
            val category = categories.get(categoryId) ?: throw DomainException.NotFound
            if (category.isSystem) throw DomainException.SystemRowNotDeletable

            val usage = categories.countUsage(categoryId)
            if (usage > 0) {
                val target = reassignToId?.let {
                    categories.get(it) ?: throw DomainException.NotFound
                } ?: throw DomainException.ReassignmentRequired(usage)

                if (target.id == category.id ||
                    target.kind != category.kind ||
                    target.isArchived
                ) {
                    throw DomainException.InvalidReassignment
                }
                categories.reassignTransactions(categoryId, target.id, appClock.nowMillis())
            }
            categories.delete(categoryId)
        }
}
