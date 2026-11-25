package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.testing.FakeDebtRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRunner
import com.appcraftbyk.expense.art.testing.debtFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class RecordRepaymentTest {

    private val debts = FakeDebtRepository()
    private val runner = FakeTransactionRunner()
    private val recordRepayment = RecordRepayment(debts, runner)

    @Before
    fun setUp() {
        debts.seed(debtFixture(id = 1, principalMinor = 10_000))
    }

    @Test
    fun partialRepayment_keepsDebtOpen() = runBlocking {
        recordRepayment(debtId = 1, amountMinor = 4_000, paidAt = 100)

        assertEquals(6_000L, debts.outstandingMinor(1))
        assertEquals(DebtStatus.OPEN, debts.getDebt(1)!!.status)
        assertEquals(1, debts.repaymentsFor(1).size)
        assertEquals(1, runner.callCount)
    }

    @Test
    fun fullRepayment_settlesDebtInSameTransaction() = runBlocking {
        recordRepayment(debtId = 1, amountMinor = 10_000, paidAt = 500)

        val debt = debts.getDebt(1)!!
        assertEquals(DebtStatus.SETTLED, debt.status)
        assertEquals(500L, debt.settledAt)
        assertEquals(0L, debts.outstandingMinor(1))
        assertEquals(1, runner.callCount)
    }

    @Test
    fun overpayment_isRejectedAndWritesNothing() {
        val error = assertThrows(DomainException.OverPayment::class.java) {
            runBlocking { recordRepayment(debtId = 1, amountMinor = 10_001, paidAt = 100) }
        }

        assertEquals(10_000L, error.outstandingMinor)
        assertEquals(0, debts.repaymentsFor(1).size)
        assertEquals(DebtStatus.OPEN, runBlocking { debts.getDebt(1) }!!.status)
    }

    @Test
    fun nonPositiveAmount_isRejected() {
        assertThrows(DomainException.InvalidAmount::class.java) {
            runBlocking { recordRepayment(debtId = 1, amountMinor = 0, paidAt = 100) }
        }
        assertEquals(0, debts.repaymentsFor(1).size)
    }

    @Test
    fun settledDebt_rejectsFurtherRepayment() {
        debts.seed(debtFixture(id = 2, status = DebtStatus.SETTLED))

        assertThrows(DomainException.AlreadySettled::class.java) {
            runBlocking { recordRepayment(debtId = 2, amountMinor = 100, paidAt = 100) }
        }
        assertEquals(0, debts.repaymentsFor(2).size)
    }

    @Test
    fun unknownDebt_isRejected() {
        assertThrows(DomainException.NotFound::class.java) {
            runBlocking { recordRepayment(debtId = 99, amountMinor = 100, paidAt = 100) }
        }
    }
}
