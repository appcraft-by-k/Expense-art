package com.appcraftbyk.expense.art.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appcraftbyk.expense.art.data.local.entity.TransactionEntity
import com.appcraftbyk.expense.art.data.local.entity.TransactionKind
import com.appcraftbyk.expense.art.data.local.projection.CategorySpend
import com.appcraftbyk.expense.art.data.local.projection.KindTotal
import com.appcraftbyk.expense.art.data.local.projection.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        "SELECT kind, SUM(amountMinor) AS totalMinor FROM transactions " +
            "WHERE occurredAt >= :from AND occurredAt < :to GROUP BY kind"
    )
    fun observeTotalsByKind(from: Long, to: Long): Flow<List<KindTotal>>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions " +
            "WHERE kind = :kind AND occurredAt >= :from AND occurredAt < :to"
    )
    fun observeTotalByKind(kind: TransactionKind, from: Long, to: Long): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions " +
            "WHERE kind = :kind AND occurredAt >= :from AND occurredAt < :to"
    )
    suspend fun totalByKind(kind: TransactionKind, from: Long, to: Long): Long

    @Query(
        "SELECT t.categoryId AS categoryId, c.name AS categoryName, c.iconKey AS categoryIconKey, " +
            "SUM(t.amountMinor) AS totalMinor " +
            "FROM transactions t LEFT JOIN categories c ON c.id = t.categoryId " +
            "WHERE t.kind = :kind AND t.occurredAt >= :from AND t.occurredAt < :to " +
            "GROUP BY t.categoryId ORDER BY totalMinor DESC LIMIT :limit"
    )
    fun observeSpendByCategory(
        kind: TransactionKind,
        from: Long,
        to: Long,
        limit: Int
    ): Flow<List<CategorySpend>>

    @Query(
        "SELECT t.id, t.kind, t.amountMinor, t.categoryId, c.name AS categoryName, " +
            "c.iconKey AS categoryIconKey, t.occurredAt, t.paymentMethodId, " +
            "pm.name AS paymentMethodName, t.note " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON c.id = t.categoryId " +
            "JOIN payment_methods pm ON pm.id = t.paymentMethodId " +
            "WHERE t.kind = :kind AND t.occurredAt >= :from AND t.occurredAt < :to " +
            "AND (:categoryId IS NULL OR t.categoryId = :categoryId) " +
            "ORDER BY t.occurredAt DESC, t.id DESC"
    )
    fun observeRange(
        kind: TransactionKind,
        from: Long,
        to: Long,
        categoryId: Long?
    ): Flow<List<TransactionWithCategory>>

    @Query(
        "SELECT t.id, t.kind, t.amountMinor, t.categoryId, c.name AS categoryName, " +
            "c.iconKey AS categoryIconKey, t.occurredAt, t.paymentMethodId, " +
            "pm.name AS paymentMethodName, t.note " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON c.id = t.categoryId " +
            "JOIN payment_methods pm ON pm.id = t.paymentMethodId " +
            "ORDER BY t.occurredAt DESC, t.id DESC LIMIT :limit"
    )
    fun observeRecent(limit: Int): Flow<List<TransactionWithCategory>>
}
