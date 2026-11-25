package com.appcraftbyk.expense.art.data.repository

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.data.local.dao.PaymentMethodDao
import com.appcraftbyk.expense.art.data.local.entity.PaymentMethodEntity
import com.appcraftbyk.expense.art.data.mapper.toDomain
import com.appcraftbyk.expense.art.data.mapper.toEntity
import com.appcraftbyk.expense.art.domain.model.NewPaymentMethod
import com.appcraftbyk.expense.art.domain.model.PaymentMethod
import com.appcraftbyk.expense.art.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMethodRepositoryImpl @Inject constructor(
    private val dao: PaymentMethodDao,
    private val appClock: AppClock
) : PaymentMethodRepository {

    override fun observeAll(includeArchived: Boolean): Flow<List<PaymentMethod>> =
        dao.observeAll(includeArchived).map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(paymentMethodId: Long): PaymentMethod? =
        dao.getById(paymentMethodId)?.toDomain()

    override suspend fun findByName(name: String, excludeId: Long): PaymentMethod? =
        dao.findByName(name, excludeId)?.toDomain()

    override suspend fun countUsage(paymentMethodId: Long): Int = dao.countUsage(paymentMethodId)

    override suspend fun nextSortOrder(): Int = dao.nextSortOrder()

    override suspend fun insert(paymentMethod: NewPaymentMethod): Long = dao.insert(
        PaymentMethodEntity(
            name = paymentMethod.name,
            iconKey = paymentMethod.iconKey,
            sortOrder = paymentMethod.sortOrder,
            isSystem = false,
            isArchived = false,
            createdAt = appClock.nowMillis()
        )
    )

    override suspend fun update(paymentMethod: PaymentMethod) =
        dao.update(paymentMethod.toEntity())

    override suspend fun reassignTransactions(
        fromPaymentMethodId: Long,
        toPaymentMethodId: Long,
        updatedAt: Long
    ) = dao.reassignTransactions(fromPaymentMethodId, toPaymentMethodId, updatedAt)

    override suspend fun delete(paymentMethodId: Long) = dao.deleteById(paymentMethodId)
}
