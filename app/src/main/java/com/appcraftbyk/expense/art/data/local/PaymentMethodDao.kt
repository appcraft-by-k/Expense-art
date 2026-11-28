package com.appcraftbyk.expense.art.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Insert
    suspend fun insert(paymentMethod: PaymentMethodEntity): Long

    @Update
    suspend fun update(paymentMethod: PaymentMethodEntity)

    @Delete
    suspend fun delete(paymentMethod: PaymentMethodEntity)

    @Query("SELECT * FROM payment_methods ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE id = :id")
    suspend fun findById(id: Long): PaymentMethodEntity?

    @Query("SELECT * FROM payment_methods WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): PaymentMethodEntity?
}
