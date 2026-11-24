package com.appcraftbyk.expense.art.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appcraftbyk.expense.art.data.local.entity.CategoryEntity
import com.appcraftbyk.expense.art.data.local.entity.TransactionKind
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query(
        "SELECT * FROM categories WHERE kind = :kind AND (:includeArchived = 1 OR isArchived = 0) " +
            "ORDER BY sortOrder, name"
    )
    fun observeByKind(kind: TransactionKind, includeArchived: Boolean = false): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind ORDER BY isArchived, sortOrder, name")
    fun observeAllByKind(kind: TransactionKind): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY kind, isArchived, sortOrder, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    /** Case-insensitive duplicate check; [excludeId] lets a rename keep its own name. */
    @Query(
        "SELECT * FROM categories WHERE kind = :kind AND LOWER(name) = LOWER(:name) " +
            "AND id != :excludeId LIMIT 1"
    )
    suspend fun findByName(kind: TransactionKind, name: String, excludeId: Long = 0): CategoryEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countUsage(categoryId: Long): Int

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    /** Used only inside a repository transaction that also archives or deletes the source. */
    @Query("UPDATE transactions SET categoryId = :toCategoryId, updatedAt = :updatedAt WHERE categoryId = :fromCategoryId")
    suspend fun reassignTransactions(fromCategoryId: Long, toCategoryId: Long, updatedAt: Long)

    @Query("UPDATE categories SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
}
