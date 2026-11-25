package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.core.time.DateRanges
import com.appcraftbyk.expense.art.domain.model.CategorySpendItem
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.model.UpcomingDueItem
import com.appcraftbyk.expense.art.testing.FakeBudgetRepository
import com.appcraftbyk.expense.art.testing.FakeDebtRepository
import com.appcraftbyk.expense.art.testing.FakeSavingsRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRepository
import com.appcraftbyk.expense.art.testing.debtFixture
import com.appcraftbyk.expense.art.testing.savingsPotFixture
import com.appcraftbyk.expense.art.testing.savingsTypeFixture
import com.appcraftbyk.expense.art.testing.transactionFixture
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class ObserveHomeSummaryTest {

    private val november = YearMonth.of(2026, 11)
    private val clock = AppClock(
        Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneId.of("Asia/Kolkata"))
    )
    private val transactions = FakeTransactionRepository()
    private val debts = FakeDebtRepository()
    private val savings = FakeSavingsRepository()
    private val budgets = FakeBudgetRepository()

    private val observeHomeSummary = ObserveHomeSummary(
        transactions = transactions,
        debts = debts,
        savings = savings,
        budgets = budgets,
        appClock = clock,
        dateRanges = DateRanges(clock)
    )

    @Before
    fun setUp() = runBlocking {
        transactions.totals = mapOf(
            TransactionKind.INCOME to 100_000,
            TransactionKind.EXPENSE to 40_000
        )
        transactions.spendByCategory = listOf(
            CategorySpendItem(1, "Food & Dining", "food", 25_000),
            CategorySpendItem(2, "Transport", "transport", 15_000)
        )
        transactions.recent = listOf(transactionFixture(id = 1))

        debts.seed(debtFixture(id = 1, principalMinor = 5_000, direction = DebtDirection.BORROWED))
        debts.seedRepayment(debtId = 1, amountMinor = 1_000)
        debts.seed(debtFixture(id = 2, principalMinor = 3_000, direction = DebtDirection.LENT))
        debts.upcomingDues = listOf(
            UpcomingDueItem(
                debtId = 2,
                contactName = "Asha",
                direction = DebtDirection.LENT,
                dueMinor = 3_000,
                dueDate = 20_400
            )
        )

        savings.seedType(savingsTypeFixture(id = 1, name = "Bank FD"))
        savings.seedPot(savingsPotFixture(id = 1, typeId = 1, typeName = "Bank FD"))
        savings.insertEntry(
            goalId = 1,
            kind = SavingsEntryKind.CONTRIBUTION,
            amountMinor = 7_500,
            occurredAt = 0,
            note = null
        )

        budgets.set(november, 50_000)
    }

    @Test
    fun combinesAggregates_andKeepsDebtDirectionsSeparate() = runBlocking {
        val summary = observeHomeSummary(november).first()

        assertEquals(november, summary.period)
        assertEquals(100_000L, summary.incomeMinor)
        assertEquals(40_000L, summary.expenseMinor)
        assertEquals(4_000L, summary.oweMinor)
        assertEquals(3_000L, summary.owedToMeMinor)
        assertEquals(7_500L, summary.totalSavingsMinor)
        assertEquals(1, summary.activeGoalCount)
        assertEquals(50_000L, summary.budgetLimitMinor)
        assertEquals(2, summary.topCategories.size)
        assertEquals(1, summary.upcomingDues.size)
        assertEquals(1, summary.recentTransactions.size)
    }

    @Test
    fun missingBudget_isNull() = runBlocking {
        budgets.clear(november)

        val summary = observeHomeSummary(november).first()

        assertEquals(null, summary.budgetLimitMinor)
    }
}
