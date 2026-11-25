package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.core.time.DateRanges
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.testing.FakeTransactionRepository
import com.appcraftbyk.expense.art.testing.transactionFixture
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class ObserveLedgerTest {

    private val clock = AppClock(
        Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneId.of("Asia/Kolkata"))
    )
    private val transactions = FakeTransactionRepository()
    private val observeLedger = ObserveLedger(transactions, DateRanges(clock))

    @Test
    fun queriesTheSelectedMonth_inIndianZone_withFilter() = runBlocking {
        transactions.ledger = listOf(transactionFixture(id = 1, categoryId = 5))

        val rows = observeLedger(
            kind = TransactionKind.EXPENSE,
            yearMonth = YearMonth.of(2026, 11),
            categoryId = 5
        ).first()

        assertEquals(1, rows.size)
        val query = transactions.lastLedgerQuery!!
        assertEquals(TransactionKind.EXPENSE, query.kind)
        assertEquals(5L, query.categoryId)
        // 2026-11-01T00:00+05:30 .. 2026-12-01T00:00+05:30, half-open.
        assertEquals(1_793_471_400_000L, query.fromMillis)
        assertEquals(1_796_063_400_000L, query.toMillisExclusive)
    }

    @Test
    fun nullCategoryMeansNoFilter() = runBlocking {
        observeLedger(TransactionKind.INCOME, YearMonth.of(2026, 11)).first()

        assertEquals(null, transactions.lastLedgerQuery!!.categoryId)
    }
}
