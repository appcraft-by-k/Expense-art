package com.appcraftbyk.expense.art.domain.repository

import com.appcraftbyk.expense.art.domain.model.CategorySpendItem
import com.appcraftbyk.expense.art.domain.model.Transaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun observeTotalsByKind(fromMillis: Long, toMillisExclusive: Long): Flow<Map<TransactionKind, Long>>

    fun observeTotalByKind(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long
    ): Flow<Long>

    fun observeSpendByCategory(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long,
        limit: Int
    ): Flow<List<CategorySpendItem>>

    fun observeRange(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long,
        categoryId: Long? = null
    ): Flow<List<Transaction>>

    fun observeRecent(limit: Int): Flow<List<Transaction>>

    suspend fun add(
        kind: TransactionKind,
        amountMinor: Long,
        categoryId: Long?,
        occurredAt: Long,
        paymentMethodId: Long,
        note: String?
    ): Long
}
