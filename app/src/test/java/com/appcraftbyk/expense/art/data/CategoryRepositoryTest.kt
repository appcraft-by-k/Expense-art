package com.appcraftbyk.expense.art.data

import com.appcraftbyk.expense.art.data.local.CategoryDao
import com.appcraftbyk.expense.art.data.local.CategoryEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CategoryRepositoryTest {

    private val fixedNow = 1_700_000_000_000L
    private val dao = FakeCategoryDao()
    private val repository = CategoryRepository(dao, now = { fixedNow })

    @Test
    fun add_trimsNameAndStoresCustomCategory() = runBlocking {
        val id = repository.add("  Coffee  ", TransactionType.EXPENSE)

        val saved = dao.inserted.single()
        assertEquals(id, saved.id)
        assertEquals("Coffee", saved.name)
        assertEquals(TransactionType.EXPENSE, saved.type)
        assertEquals(false, saved.isDefault)
        assertEquals(fixedNow, saved.createdAt)
    }

    @Test
    fun add_rejectsBlankName() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.add("   ", TransactionType.INCOME) }
        }
    }

    @Test
    fun add_rejectsDuplicateName() {
        runBlocking { repository.add("Coffee", TransactionType.EXPENSE) }

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.add("  coffee  ", TransactionType.EXPENSE) }
        }
    }

    @Test
    fun delete_rejectsCategoryInUse() {
        runBlocking { repository.add("Coffee", TransactionType.EXPENSE) }
        dao.transactionCount = 1
        val saved = dao.inserted.single()

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.delete(saved) }
        }
    }

    @Test
    fun delete_removesUnusedCategory() = runBlocking {
        repository.add("Coffee", TransactionType.EXPENSE)
        val saved = dao.inserted.single()

        repository.delete(saved)

        assertEquals(emptyList<CategoryEntity>(), dao.inserted)
    }

    private class FakeCategoryDao : CategoryDao {
        val inserted = mutableListOf<CategoryEntity>()
        var transactionCount = 0

        override suspend fun insert(category: CategoryEntity): Long {
            val stored = category.copy(id = inserted.size + 1L)
            inserted += stored
            return stored.id
        }

        override suspend fun update(category: CategoryEntity) {
            inserted.replaceAll { if (it.id == category.id) category else it }
        }

        override suspend fun delete(category: CategoryEntity) {
            inserted.removeAll { it.id == category.id }
        }

        override fun observeAll(): Flow<List<CategoryEntity>> = flowOf(inserted)

        override fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> =
            flowOf(inserted.filter { it.type == type })

        override suspend fun findById(id: Long): CategoryEntity? =
            inserted.find { it.id == id }

        override suspend fun findByName(name: String, type: TransactionType): CategoryEntity? =
            inserted.find { it.name.equals(name, ignoreCase = true) && it.type == type }

        override suspend fun countTransactions(categoryId: Long): Int = transactionCount
    }
}
