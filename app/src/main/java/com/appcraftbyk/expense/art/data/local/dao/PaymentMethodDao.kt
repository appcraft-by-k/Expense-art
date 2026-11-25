package com.appcraftbyk.expense.art.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appcraftbyk.expense.art.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query(
        "SELECT * FROM payment_methods WHERE (:includeArchived = 1 OR isArchived = 0) " +
            "ORDER BY sortOrder, name"
    )
    fun observeAll(includeArchived: Boolean = false): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE id = :id")
    suspend fun getById(id: Long): PaymentMethodEntity?

    /** Case-insensitive duplicate check; [excludeId] lets a rename keep its own name. */
    @Query("SELECT * FROM payment_methods WHERE LOWER(name) = LOWER(:name) AND id != :excludeId LIMIT 1")
    suspend fun findByName(name: String, excludeId: Long = 0): PaymentMethodEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE paymentMethodId = :paymentMethodId")
    suspend fun countUsage(paymentMethodId: Long): Int

    @Insert
    suspend fun insert(paymentMethod: PaymentMethodEntity): Long

    @Update
    suspend fun update(paymentMethod: PaymentMethodEntity)

    @Delete
    suspend fun delete(paymentMethod: PaymentMethodEntity)

    /** Used only inside a repository transaction that also archives or deletes the source. */
    @Query(
        "UPDATE transactions SET paymentMethodId = :toPaymentMethodId, updatedAt = :updatedAt " +
            "WHERE paymentMethodId = :fromPaymentMethodId"
    )
    suspend fun reassignTransactions(
        fromPaymentMethodId: Long,
        toPaymentMethodId: Long,
        updatedAt: Long
    )

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM payment_methods")
    suspend fun nextSortOrder(): Int

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun deleteById(id: Long)
}
