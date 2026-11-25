package com.appcraftbyk.expense.art.domain.repository

import com.appcraftbyk.expense.art.domain.model.Category
import com.appcraftbyk.expense.art.domain.model.NewCategory
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeByKind(kind: TransactionKind, includeArchived: Boolean = false): Flow<List<Category>>

    fun observeAllByKind(kind: TransactionKind): Flow<List<Category>>

    fun observeAll(): Flow<List<Category>>

    suspend fun get(categoryId: Long): Category?

    /** Case-insensitive duplicate check; [excludeId] lets a rename keep its own name. */
    suspend fun findByName(kind: TransactionKind, name: String, excludeId: Long = 0): Category?

    suspend fun countUsage(categoryId: Long): Int

    suspend fun nextSortOrder(kind: TransactionKind): Int

    suspend fun insert(category: NewCategory): Long

    /** Updates labels and archive state; [Category.uid] and [Category.isSystem] must survive. */
    suspend fun update(category: Category)

    suspend fun reassignTransactions(fromCategoryId: Long, toCategoryId: Long, updatedAt: Long)

    suspend fun delete(categoryId: Long)
}
