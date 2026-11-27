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
        category: String,
        occurredAt: Long = now(),
        note: String? = null,
        paymentMethod: String? = null
    ): Long = add(TransactionType.EXPENSE, amountMinor, category, occurredAt, note, paymentMethod)

    suspend fun addIncome(
        amountMinor: Long,
        category: String,
        occurredAt: Long = now(),
        note: String? = null,
        paymentMethod: String? = null
    ): Long = add(TransactionType.INCOME, amountMinor, category, occurredAt, note, paymentMethod)

    suspend fun update(transaction: TransactionEntity) {
        require(transaction.amountMinor > 0) { "Amount must be greater than zero" }
        require(transaction.category.isNotBlank()) { "Category must not be blank" }
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    private suspend fun add(
        type: TransactionType,
        amountMinor: Long,
        category: String,
        occurredAt: Long,
        note: String?,
        paymentMethod: String?
    ): Long {
        require(amountMinor > 0) { "Amount must be greater than zero" }
        val trimmedCategory = category.trim()
        require(trimmedCategory.isNotEmpty()) { "Category must not be blank" }

        return transactionDao.insert(
            TransactionEntity(
                type = type,
                amountMinor = amountMinor,
                category = trimmedCategory,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                paymentMethod = paymentMethod?.trim()?.takeIf { it.isNotEmpty() },
                occurredAt = occurredAt,
                createdAt = now()
            )
        )
    }
}
