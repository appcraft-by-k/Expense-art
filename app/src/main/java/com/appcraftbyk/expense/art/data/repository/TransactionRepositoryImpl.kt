package com.appcraftbyk.expense.art.data.repository

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.data.local.dao.TransactionDao
import com.appcraftbyk.expense.art.data.local.entity.TransactionEntity
import com.appcraftbyk.expense.art.data.mapper.toDomain
import com.appcraftbyk.expense.art.domain.model.CategorySpendItem
import com.appcraftbyk.expense.art.domain.model.Transaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val appClock: AppClock
) : TransactionRepository {

    override fun observeTotalsByKind(
        fromMillis: Long,
        toMillisExclusive: Long
    ): Flow<Map<TransactionKind, Long>> =
        dao.observeTotalsByKind(fromMillis, toMillisExclusive)
            .map { rows -> rows.associate { it.kind to it.totalMinor } }

    override fun observeTotalByKind(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long
    ): Flow<Long> = dao.observeTotalByKind(kind, fromMillis, toMillisExclusive)

    override fun observeSpendByCategory(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long,
        limit: Int
    ): Flow<List<CategorySpendItem>> =
        dao.observeSpendByCategory(kind, fromMillis, toMillisExclusive, limit)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeRange(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long,
        categoryId: Long?
    ): Flow<List<Transaction>> =
        dao.observeRange(kind, fromMillis, toMillisExclusive, categoryId)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }

    override suspend fun add(
        kind: TransactionKind,
        amountMinor: Long,
        categoryId: Long?,
        occurredAt: Long,
        paymentMethodId: Long,
        note: String?
    ): Long {
        val now = appClock.nowMillis()
        return dao.insert(
            TransactionEntity(
                kind = kind,
                amountMinor = amountMinor,
                categoryId = categoryId,
                occurredAt = occurredAt,
                paymentMethodId = paymentMethodId,
                note = note,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
