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

class SettleDebtTest {

    private val debts = FakeDebtRepository()
    private val runner = FakeTransactionRunner()
    private val settleDebt = SettleDebt(debts, runner)

    @Before
    fun setUp() {
        debts.seed(debtFixture(id = 1, principalMinor = 10_000))
        debts.seedRepayment(debtId = 1, amountMinor = 3_000)
    }

    @Test
    fun settlesRemainingBalance_withOneRepaymentRow() = runBlocking {
        settleDebt(debtId = 1, settledAt = 777)

        val debt = debts.getDebt(1)!!
        assertEquals(DebtStatus.SETTLED, debt.status)
        assertEquals(777L, debt.settledAt)
        assertEquals(0L, debts.outstandingMinor(1))
        assertEquals(2, debts.repaymentsFor(1).size)
        assertEquals(7_000L, debts.repaymentsFor(1).last().amountMinor)
        assertEquals(1, runner.callCount)
    }

    @Test
    fun alreadySettled_isIdempotent() = runBlocking {
        debts.seed(debtFixture(id = 2, status = DebtStatus.SETTLED))

        settleDebt(debtId = 2, settledAt = 900)

        assertEquals(0, debts.repaymentsFor(2).size)
        assertEquals(DebtStatus.SETTLED, debts.getDebt(2)!!.status)
    }

    @Test
    fun unknownDebt_isRejected() {
        assertThrows(DomainException.NotFound::class.java) {
            runBlocking { settleDebt(debtId = 99, settledAt = 1) }
        }
    }
}
