package com.appcraftbyk.expense.art.data

import com.appcraftbyk.expense.art.data.local.CategoryDao
import com.appcraftbyk.expense.art.data.local.CategoryEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import kotlinx.coroutines.flow.Flow

/** User-facing catalog of expense and income categories. */
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val now: () -> Long = System::currentTimeMillis
) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.observeByType(type)

    suspend fun findById(id: Long): CategoryEntity? = categoryDao.findById(id)

    suspend fun add(name: String, type: TransactionType): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name must not be blank" }
        require(categoryDao.findByName(trimmed, type) == null) {
            "Category already exists"
        }

        return categoryDao.insert(
            CategoryEntity(
                name = trimmed,
                type = type,
                createdAt = now()
            )
        )
    }

    suspend fun update(category: CategoryEntity) {
        val stored = categoryDao.findById(category.id)
            ?: throw IllegalArgumentException("Category not found")

        val trimmed = category.name.trim()
        require(trimmed.isNotEmpty()) { "Category name must not be blank" }
        require(stored.type == category.type) { "Category type cannot be changed" }

        val duplicate = categoryDao.findByName(trimmed, category.type)
        require(duplicate == null || duplicate.id == category.id) {
            "Category already exists"
        }

        categoryDao.update(category.copy(name = trimmed))
    }

    suspend fun delete(category: CategoryEntity) {
        require(categoryDao.countTransactions(category.id) == 0) {
            "Category is in use by existing transactions"
        }
        categoryDao.delete(category)
    }
}
