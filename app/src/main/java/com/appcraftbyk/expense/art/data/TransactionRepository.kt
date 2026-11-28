package com.appcraftbyk.expense.art.data

import com.appcraftbyk.expense.art.data.local.TransactionDao
import com.appcraftbyk.expense.art.data.local.TransactionEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for expense and income records.
 * Amounts are always positive minor units; [TransactionType] carries the direction.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val now: () -> Long = System::currentTimeMillis
) {

    fun observeAll(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeByType(type: TransactionType): Flow<List<TransactionEntity>> =
        transactionDao.observeByType(type)

    fun observeTotal(type: TransactionType, from: Long, until: Long): Flow<Long> =
        transactionDao.observeTotal(type, from, until)

    suspend fun findById(id: Long): TransactionEntity? = transactionDao.findById(id)

    suspend fun addExpense(
        amountMinor: Long,
        categoryId: Long,
        occurredAt: Long = now(),
        note: String? = null,
        paymentMethodId: Long? = null
    ): Long = add(TransactionType.EXPENSE, amountMinor, categoryId, occurredAt, note, paymentMethodId)

    suspend fun addIncome(
        amountMinor: Long,
        categoryId: Long,
        occurredAt: Long = now(),
        note: String? = null,
        paymentMethodId: Long? = null
    ): Long = add(TransactionType.INCOME, amountMinor, categoryId, occurredAt, note, paymentMethodId)

    suspend fun update(transaction: TransactionEntity) {
        requireValid(transaction.amountMinor, transaction.categoryId, transaction.paymentMethodId)
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    private suspend fun add(
        type: TransactionType,
        amountMinor: Long,
        categoryId: Long,
        occurredAt: Long,
        note: String?,
        paymentMethodId: Long?
    ): Long {
        requireValid(amountMinor, categoryId, paymentMethodId)

        return transactionDao.insert(
            TransactionEntity(
                type = type,
                amountMinor = amountMinor,
                categoryId = categoryId,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                paymentMethodId = paymentMethodId,
                occurredAt = occurredAt,
                createdAt = now()
            )
        )
    }

    private fun requireValid(amountMinor: Long, categoryId: Long, paymentMethodId: Long?) {
        require(amountMinor > 0) { "Amount must be greater than zero" }
        require(categoryId > 0) { "A category is required" }
        require(paymentMethodId == null || paymentMethodId > 0) {
            "Payment method id must be positive"
        }
    }
}
