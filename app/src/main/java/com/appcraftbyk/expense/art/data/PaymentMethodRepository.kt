package com.appcraftbyk.expense.art.data

import com.appcraftbyk.expense.art.data.local.PaymentMethodDao
import com.appcraftbyk.expense.art.data.local.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

/** User-facing catalog of payment methods. */
class PaymentMethodRepository(
    private val paymentMethodDao: PaymentMethodDao,
    private val now: () -> Long = System::currentTimeMillis
) {

    fun observeAll(): Flow<List<PaymentMethodEntity>> = paymentMethodDao.observeAll()

    suspend fun findById(id: Long): PaymentMethodEntity? = paymentMethodDao.findById(id)

    suspend fun add(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Payment method name must not be blank" }
        require(paymentMethodDao.findByName(trimmed) == null) {
            "Payment method already exists"
        }

        return paymentMethodDao.insert(
            PaymentMethodEntity(
                name = trimmed,
                createdAt = now()
            )
        )
    }

    suspend fun update(paymentMethod: PaymentMethodEntity) {
        paymentMethodDao.findById(paymentMethod.id)
            ?: throw IllegalArgumentException("Payment method not found")

        val trimmed = paymentMethod.name.trim()
        require(trimmed.isNotEmpty()) { "Payment method name must not be blank" }

        val duplicate = paymentMethodDao.findByName(trimmed)
        require(duplicate == null || duplicate.id == paymentMethod.id) {
            "Payment method already exists"
        }

        paymentMethodDao.update(paymentMethod.copy(name = trimmed))
    }

    suspend fun delete(paymentMethod: PaymentMethodEntity) =
        paymentMethodDao.delete(paymentMethod)
}
