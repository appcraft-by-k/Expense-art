package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.testing.FakeSavingsRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRunner
import com.appcraftbyk.expense.art.testing.savingsPotFixture
import com.appcraftbyk.expense.art.testing.savingsTypeFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/** Invariant 8: savings-type rules mirror categories. */
class SavingsTypeRulesTest {

    private val savings = FakeSavingsRepository()
    private val runner = FakeTransactionRunner()
    private val deleteSavingsType = DeleteSavingsType(savings, runner)
    private val saveSavingsType = SaveSavingsType(savings)

    @Before
    fun setUp() {
        savings.seedType(
            savingsTypeFixture(id = 1, name = "Bank FD", isSystem = true, uid = "sys-bank-fd")
        )
        savings.seedType(savingsTypeFixture(id = 2, name = "Gold"))
        savings.seedType(savingsTypeFixture(id = 3, name = "Archived", isArchived = true))
        savings.seedPot(savingsPotFixture(id = 1, typeId = 2, typeName = "Gold"))
    }

    @Test
    fun systemType_cannotBeDeleted() {
        assertThrows(DomainException.SystemRowNotDeletable::class.java) {
            runBlocking { deleteSavingsType(typeId = 1) }
        }
        assertEquals("Bank FD", savings.type(1)?.name)
    }

    @Test
    fun unusedUserType_isDeleted() = runBlocking {
        deleteSavingsType(typeId = 3)

        assertNull(savings.type(3))
    }

    @Test
    fun usedType_withoutTarget_requiresReassignment() {
        val error = assertThrows(DomainException.ReassignmentRequired::class.java) {
            runBlocking { deleteSavingsType(typeId = 2) }
        }

        assertEquals(1, error.usageCount)
        assertEquals("Gold", savings.type(2)?.name)
        assertNull(savings.lastReassignedType)
    }

    @Test
    fun usedType_withArchivedTarget_isRejected() {
        savings.seedType(savingsTypeFixture(id = 5, name = "Old", isArchived = true))

        assertThrows(DomainException.InvalidReassignment::class.java) {
            runBlocking { deleteSavingsType(typeId = 2, reassignToId = 5) }
        }
        assertEquals("Gold", savings.type(2)?.name)
        assertNull(savings.lastReassignedType)
    }

    @Test
    fun usedType_withActiveTarget_reassignsPotsAndDeletes() = runBlocking {
        savings.seedType(savingsTypeFixture(id = 4, name = "Cash"))

        deleteSavingsType(typeId = 2, reassignToId = 4)

        assertEquals(2L to 4L, savings.lastReassignedType)
        assertNull(savings.type(2))
        assertEquals(4L, savings.pot(1)?.typeId)
        assertEquals("Cash", savings.pot(1)?.typeName)
        assertEquals(1, runner.callCount)
    }

    @Test
    fun duplicateName_isRejectedCaseInsensitively() {
        assertThrows(DomainException.DuplicateName::class.java) {
            runBlocking {
                saveSavingsType(typeId = null, name = "bank fd", iconKey = "bank_fd")
            }
        }
    }

    @Test
    fun edit_preservesUidAndSystemFlag() = runBlocking {
        saveSavingsType(typeId = 1, name = "Fixed Deposit", iconKey = "bank_fd")

        val updated = savings.type(1)!!
        assertEquals("Fixed Deposit", updated.name)
        assertEquals("sys-bank-fd", updated.uid)
        assertEquals(true, updated.isSystem)
    }
}
