package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.testing.FakeSavingsRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRunner
import com.appcraftbyk.expense.art.testing.savingsPotFixture
import com.appcraftbyk.expense.art.testing.savingsTypeFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ContributeToPotTest {

    private val savings = FakeSavingsRepository()
    private val runner = FakeTransactionRunner()
    private val clock = AppClock(
        Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneId.of("Asia/Kolkata"))
    )
    private val contribute = ContributeToPot(savings, runner, clock)

    @Before
    fun setUp() {
        savings.seedType(savingsTypeFixture(id = 1, name = "Bank FD"))
        savings.seedPot(savingsPotFixture(id = 1, typeId = 1, typeName = "Bank FD"))
        runBlocking {
            savings.insertEntry(
                goalId = 1,
                kind = SavingsEntryKind.CONTRIBUTION,
                amountMinor = 5_000,
                occurredAt = 0,
                note = null
            )
        }
    }

    @Test
    fun contribution_increasesBalance() = runBlocking {
        contribute(goalId = 1, kind = SavingsEntryKind.CONTRIBUTION, amountMinor = 2_000)

        assertEquals(7_000L, savings.potBalanceMinor(1))
        assertEquals(2, savings.entriesFor(1).size)
        assertEquals(clock.nowMillis(), savings.entriesFor(1).last().occurredAt)
        assertEquals(1, runner.callCount)
    }

    @Test
    fun withdrawalAboveBalance_isRejected() {
        val error = assertThrows(DomainException.WithdrawalExceedsBalance::class.java) {
            runBlocking {
                contribute(goalId = 1, kind = SavingsEntryKind.WITHDRAWAL, amountMinor = 5_001)
            }
        }

        assertEquals(5_000L, error.balanceMinor)
        assertEquals(1, savings.entriesFor(1).size)
    }

    @Test
    fun withdrawalOfFullBalance_isAllowed() = runBlocking {
        contribute(goalId = 1, kind = SavingsEntryKind.WITHDRAWAL, amountMinor = 5_000)

        assertEquals(0L, savings.potBalanceMinor(1))
        assertEquals(2, savings.entriesFor(1).size)
    }

    @Test
    fun nonPositiveAmount_isRejected() {
        assertThrows(DomainException.InvalidAmount::class.java) {
            runBlocking {
                contribute(goalId = 1, kind = SavingsEntryKind.CONTRIBUTION, amountMinor = 0)
            }
        }
        assertEquals(1, savings.entriesFor(1).size)
    }

    @Test
    fun archivedPot_isRejected() {
        savings.seedPot(savingsPotFixture(id = 2, isArchived = true))

        assertThrows(DomainException.ArchivedTarget::class.java) {
            runBlocking {
                contribute(goalId = 2, kind = SavingsEntryKind.CONTRIBUTION, amountMinor = 100)
            }
        }
        assertEquals(0, savings.entriesFor(2).size)
    }
}
