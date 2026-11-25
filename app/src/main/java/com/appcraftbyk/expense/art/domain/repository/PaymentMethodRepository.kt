package com.appcraftbyk.expense.art.domain.repository

import com.appcraftbyk.expense.art.domain.model.NewPaymentMethod
import com.appcraftbyk.expense.art.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {

    fun observeAll(includeArchived: Boolean = false): Flow<List<PaymentMethod>>

    suspend fun get(paymentMethodId: Long): PaymentMethod?

    /** Case-insensitive duplicate check; [excludeId] lets a rename keep its own name. */
    suspend fun findByName(name: String, excludeId: Long = 0): PaymentMethod?

    suspend fun countUsage(paymentMethodId: Long): Int

    suspend fun nextSortOrder(): Int

    suspend fun insert(paymentMethod: NewPaymentMethod): Long

    /** Updates labels and archive state; [PaymentMethod.uid] and [PaymentMethod.isSystem] survive. */
    suspend fun update(paymentMethod: PaymentMethod)

    suspend fun reassignTransactions(
        fromPaymentMethodId: Long,
        toPaymentMethodId: Long,
        updatedAt: Long
    )

    suspend fun delete(paymentMethodId: Long)
}
