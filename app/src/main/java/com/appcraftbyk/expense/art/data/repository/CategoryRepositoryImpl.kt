package com.appcraftbyk.expense.art.data.repository

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.data.local.dao.CategoryDao
import com.appcraftbyk.expense.art.data.local.entity.CategoryEntity
import com.appcraftbyk.expense.art.data.mapper.toDomain
import com.appcraftbyk.expense.art.data.mapper.toEntity
import com.appcraftbyk.expense.art.domain.model.Category
import com.appcraftbyk.expense.art.domain.model.NewCategory
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val appClock: AppClock
) : CategoryRepository {

    override fun observeByKind(kind: TransactionKind, includeArchived: Boolean): Flow<List<Category>> =
        dao.observeByKind(kind, includeArchived).map { rows -> rows.map { it.toDomain() } }

    override fun observeAllByKind(kind: TransactionKind): Flow<List<Category>> =
        dao.observeAllByKind(kind).map { rows -> rows.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun get(categoryId: Long): Category? = dao.getById(categoryId)?.toDomain()

    override suspend fun findByName(
        kind: TransactionKind,
        name: String,
        excludeId: Long
    ): Category? = dao.findByName(kind, name, excludeId)?.toDomain()

    override suspend fun countUsage(categoryId: Long): Int = dao.countUsage(categoryId)

    override suspend fun nextSortOrder(kind: TransactionKind): Int = dao.nextSortOrder(kind)

    override suspend fun insert(category: NewCategory): Long = dao.insert(
        CategoryEntity(
            name = category.name,
            kind = category.kind,
            iconKey = category.iconKey,
            sortOrder = category.sortOrder,
            isSystem = false,
            isArchived = false,
            createdAt = appClock.nowMillis()
        )
    )

    override suspend fun update(category: Category) = dao.update(category.toEntity())

    override suspend fun reassignTransactions(
        fromCategoryId: Long,
        toCategoryId: Long,
        updatedAt: Long
    ) = dao.reassignTransactions(fromCategoryId, toCategoryId, updatedAt)

    override suspend fun delete(categoryId: Long) = dao.deleteById(categoryId)
}
