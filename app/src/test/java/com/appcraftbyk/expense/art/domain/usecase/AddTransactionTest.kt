package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.NewTransaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.testing.FakeCategoryRepository
import com.appcraftbyk.expense.art.testing.FakePaymentMethodRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRepository
import com.appcraftbyk.expense.art.testing.categoryFixture
import com.appcraftbyk.expense.art.testing.paymentMethodFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class AddTransactionTest {

    private val fixedInstant = Instant.parse("2026-11-15T10:00:00Z")
    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository()
    private val paymentMethods = FakePaymentMethodRepository()
    private val clock = AppClock(Clock.fixed(fixedInstant, ZoneId.of("Asia/Kolkata")))
    private val addTransaction = AddTransaction(transactions, categories, paymentMethods, clock)

    @Before
    fun setUp() {
        categories.seed(categoryFixture(id = 1, kind = TransactionKind.EXPENSE))
        categories.seed(categoryFixture(id = 2, kind = TransactionKind.INCOME))
        categories.seed(categoryFixture(id = 3, kind = TransactionKind.EXPENSE, isArchived = true))
        paymentMethods.seed(paymentMethodFixture(id = 1, name = "UPI"))
        paymentMethods.seed(paymentMethodFixture(id = 2, name = "Old", isArchived = true))
    }

    @Test
    fun validExpense_defaultsToNowAndTrimsNote() = runBlocking {
        addTransaction(
            NewTransaction(
                kind = TransactionKind.EXPENSE,
                amountMinor = 25_000,
                categoryId = 1,
                paymentMethodId = 1,
                note = "  chai  "
            )
        )

        val added = transactions.added.single()
        assertEquals(25_000L, added.amountMinor)
        assertEquals(1L, added.categoryId)
        assertEquals(1L, added.paymentMethodId)
        assertEquals("chai", added.note)
        assertEquals(fixedInstant.toEpochMilli(), added.occurredAt)
    }

    @Test
    fun incomeMayHaveNoCategory() = runBlocking {
        addTransaction(
            NewTransaction(
                kind = TransactionKind.INCOME,
                amountMinor = 1_000_00,
                categoryId = null,
                paymentMethodId = 1
            )
        )

        assertEquals(null, transactions.added.single().categoryId)
    }

    @Test
    fun nonPositiveAmount_isRejected() {
        assertThrows(DomainException.InvalidAmount::class.java) {
            runBlocking {
                addTransaction(
                    NewTransaction(TransactionKind.EXPENSE, 0, 1, 1)
                )
            }
        }
        assertEquals(0, transactions.added.size)
    }

    @Test
    fun categoryFromOtherLedger_isRejected() {
        assertThrows(DomainException.CategoryKindMismatch::class.java) {
            runBlocking {
                addTransaction(
                    NewTransaction(TransactionKind.INCOME, 100, categoryId = 1, paymentMethodId = 1)
                )
            }
        }
        assertEquals(0, transactions.added.size)
    }

    @Test
    fun archivedCategory_isRejected() {
        assertThrows(DomainException.ArchivedTarget::class.java) {
            runBlocking {
                addTransaction(
                    NewTransaction(TransactionKind.EXPENSE, 100, categoryId = 3, paymentMethodId = 1)
                )
            }
        }
    }

    @Test
    fun archivedPaymentMethod_isRejected() {
        assertThrows(DomainException.ArchivedTarget::class.java) {
            runBlocking {
                addTransaction(
                    NewTransaction(TransactionKind.EXPENSE, 100, categoryId = 1, paymentMethodId = 2)
                )
            }
        }
    }

    @Test
    fun unknownCategoryOrMethod_isRejected() {
        assertThrows(DomainException.NotFound::class.java) {
            runBlocking {
                addTransaction(
                    NewTransaction(TransactionKind.EXPENSE, 100, categoryId = 99, paymentMethodId = 1)
                )
            }
        }
        assertThrows(DomainException.NotFound::class.java) {
            runBlocking {
                addTransaction(
                    NewTransaction(TransactionKind.EXPENSE, 100, categoryId = 1, paymentMethodId = 99)
                )
            }
        }
    }
}
