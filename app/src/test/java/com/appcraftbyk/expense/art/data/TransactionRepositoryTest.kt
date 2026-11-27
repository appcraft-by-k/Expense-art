package com.appcraftbyk.expense.art.data

import com.appcraftbyk.expense.art.data.local.TransactionDao
import com.appcraftbyk.expense.art.data.local.TransactionEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TransactionRepositoryTest {

    private val fixedNow = 1_700_000_000_000L
    private val dao = FakeTransactionDao()
    private val repository = TransactionRepository(dao, now = { fixedNow })

    @Test
    fun addExpense_storesExpenseEntry() = runBlocking {
        val id = repository.addExpense(
            amountMinor = 184_050,
            category = "  Groceries  ",
            occurredAt = fixedNow - 1_000,
            note = "  ",
            paymentMethod = "UPI"
        )

        val saved = dao.inserted.single()
        assertEquals(id, saved.id)
        assertEquals(TransactionType.EXPENSE, saved.type)
        assertEquals(184_050L, saved.amountMinor)
        assertEquals("Groceries", saved.category)
        assertEquals("UPI", saved.paymentMethod)
        assertEquals(null, saved.note)
        assertEquals(fixedNow - 1_000, saved.occurredAt)
        assertEquals(fixedNow, saved.createdAt)
    }

    @Test
    fun addIncome_storesIncomeEntry() = runBlocking {
        repository.addIncome(amountMinor = 2_500_000, category = "Freelance")

        val saved = dao.inserted.single()
        assertEquals(TransactionType.INCOME, saved.type)
        assertEquals(2_500_000L, saved.amountMinor)
        assertEquals(fixedNow, saved.occurredAt)
    }

    @Test
    fun addExpense_rejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.addExpense(amountMinor = 0, category = "Food") }
        }
    }

    @Test
    fun addIncome_rejectsBlankCategory() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.addIncome(amountMinor = 100, category = "   ") }
        }
    }

    private class FakeTransactionDao : TransactionDao {
        val inserted = mutableListOf<TransactionEntity>()

        override suspend fun insert(transaction: TransactionEntity): Long {
            val stored = transaction.copy(id = inserted.size + 1L)
            inserted += stored
            return stored.id
        }

        override suspend fun update(transaction: TransactionEntity) {
            inserted.replaceAll { if (it.id == transaction.id) transaction else it }
        }

        override suspend fun delete(transaction: TransactionEntity) {
            inserted.removeAll { it.id == transaction.id }
        }

        override fun observeAll(): Flow<List<TransactionEntity>> = flowOf(inserted)

        override fun observeByType(type: TransactionType): Flow<List<TransactionEntity>> =
            flowOf(inserted.filter { it.type == type })

        override suspend fun findById(id: Long): TransactionEntity? =
            inserted.find { it.id == id }

        override fun observeTotal(type: TransactionType, from: Long, until: Long): Flow<Long> =
            flowOf(
                inserted
                    .filter { it.type == type && it.occurredAt in from until until }
                    .sumOf { it.amountMinor }
            )
    }
}
