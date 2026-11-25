package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.testing.FakeBudgetRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.YearMonth

class SetBudgetTest {

    private val budgets = FakeBudgetRepository()
    private val setBudget = SetBudget(budgets)
    private val november = YearMonth.of(2026, 11)

    @Test
    fun positiveLimit_isStored() = runBlocking {
        setBudget(november, 5_000_000)

        assertEquals(5_000_000L, budgets.get(november)?.limitMinor)
        assertEquals(november, budgets.get(november)?.yearMonth)
    }

    @Test
    fun replacingExistingLimit_keepsOneRow() = runBlocking {
        setBudget(november, 1_000_000)
        setBudget(november, 2_000_000)

        assertEquals(2_000_000L, budgets.get(november)?.limitMinor)
    }

    @Test
    fun zeroOrNull_clearsBudget() = runBlocking {
        setBudget(november, 1_000_000)

        setBudget(november, 0)
        assertNull(budgets.get(november))

        setBudget(november, 1_000_000)
        setBudget(november, null)
        assertNull(budgets.get(november))
    }

    @Test
    fun negativeLimit_isRejected() {
        assertThrows(DomainException.InvalidAmount::class.java) {
            runBlocking { setBudget(november, -1) }
        }
        runBlocking { assertNull(budgets.get(november)) }
    }
}
