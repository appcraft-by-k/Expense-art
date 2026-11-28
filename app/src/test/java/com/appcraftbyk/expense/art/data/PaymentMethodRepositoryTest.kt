package com.appcraftbyk.expense.art.data

import com.appcraftbyk.expense.art.data.local.PaymentMethodDao
import com.appcraftbyk.expense.art.data.local.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PaymentMethodRepositoryTest {

    private val fixedNow = 1_700_000_000_000L
    private val dao = FakePaymentMethodDao()
    private val repository = PaymentMethodRepository(dao, now = { fixedNow })

    @Test
    fun add_trimsNameAndStoresCustomMethod() = runBlocking {
        val id = repository.add("  Cheque  ")

        val saved = dao.inserted.single()
        assertEquals(id, saved.id)
        assertEquals("Cheque", saved.name)
        assertEquals(false, saved.isDefault)
        assertEquals(fixedNow, saved.createdAt)
    }

    @Test
    fun add_rejectsBlankName() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.add("   ") }
        }
    }

    @Test
    fun add_rejectsDuplicateName() {
        runBlocking { repository.add("Cheque") }

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.add("  cheque  ") }
        }
    }

    @Test
    fun update_trimsName() = runBlocking {
        repository.add("Cheque")
        val saved = dao.inserted.single()

        repository.update(saved.copy(name = "  Bank Cheque  "))

        assertEquals("Bank Cheque", dao.inserted.single().name)
    }

    private class FakePaymentMethodDao : PaymentMethodDao {
        val inserted = mutableListOf<PaymentMethodEntity>()

        override suspend fun insert(paymentMethod: PaymentMethodEntity): Long {
            val stored = paymentMethod.copy(id = inserted.size + 1L)
            inserted += stored
            return stored.id
        }

        override suspend fun update(paymentMethod: PaymentMethodEntity) {
            inserted.replaceAll { if (it.id == paymentMethod.id) paymentMethod else it }
        }

        override suspend fun delete(paymentMethod: PaymentMethodEntity) {
            inserted.removeAll { it.id == paymentMethod.id }
        }

        override fun observeAll(): Flow<List<PaymentMethodEntity>> = flowOf(inserted)

        override suspend fun findById(id: Long): PaymentMethodEntity? =
            inserted.find { it.id == id }

        override suspend fun findByName(name: String): PaymentMethodEntity? =
            inserted.find { it.name.equals(name, ignoreCase = true) }
    }
}
