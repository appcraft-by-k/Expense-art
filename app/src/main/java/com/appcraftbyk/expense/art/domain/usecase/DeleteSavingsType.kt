package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject

/**
 * Deletes a user savings type. Used types must be reassigned (pots moved to another active type)
 * inside the same transaction; seeded types can only be archived.
 */
class DeleteSavingsType @Inject constructor(
    private val savings: SavingsRepository,
    private val transactionRunner: TransactionRunner
) {

    suspend operator fun invoke(typeId: Long, reassignToId: Long? = null) =
        transactionRunner.inTransaction {
            val type = savings.getType(typeId) ?: throw DomainException.NotFound
            if (type.isSystem) throw DomainException.SystemRowNotDeletable

            val usage = savings.countTypeUsage(typeId)
            if (usage > 0) {
                val target = reassignToId?.let {
                    savings.getType(it) ?: throw DomainException.NotFound
                } ?: throw DomainException.ReassignmentRequired(usage)

                if (target.id == type.id || target.isArchived) {
                    throw DomainException.InvalidReassignment
                }
                savings.reassignGoals(typeId, target.id)
            }
            savings.deleteType(typeId)
        }
}
