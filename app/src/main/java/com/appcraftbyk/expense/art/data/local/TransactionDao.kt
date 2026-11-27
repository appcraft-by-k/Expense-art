package com.appcraftbyk.expense.art.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY occurred_at DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE type = :type " +
            "ORDER BY occurred_at DESC, id DESC"
    )
    fun observeByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: Long): TransactionEntity?

    /** Total for a half-open range `[from, until)` in minor units. */
    @Query(
        "SELECT COALESCE(SUM(amount_minor), 0) FROM transactions " +
            "WHERE type = :type AND occurred_at >= :from AND occurred_at < :until"
    )
    fun observeTotal(type: TransactionType, from: Long, until: Long): Flow<Long>
}
