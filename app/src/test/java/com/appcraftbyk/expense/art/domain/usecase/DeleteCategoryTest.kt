package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.testing.FakeCategoryRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRunner
import com.appcraftbyk.expense.art.testing.categoryFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DeleteCategoryTest {

    private val categories = FakeCategoryRepository()
    private val runner = FakeTransactionRunner()
    private val clock = AppClock(
        Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneId.of("Asia/Kolkata"))
    )
    private val deleteCategory = DeleteCategory(categories, runner, clock)

    @Before
    fun setUp() {
        categories.seed(categoryFixture(id = 1, name = "Food", isSystem = true, uid = "sys-food"))
        categories.seed(categoryFixture(id = 2, name = "Cafe"))
        categories.seed(categoryFixture(id = 3, name = "Travel"))
        categories.seed(categoryFixture(id = 4, name = "Gym"))
        categories.seed(categoryFixture(id = 5, name = "Old"))
        categories.seed(categoryFixture(id = 6, name = "New"))
        categories.seed(
            categoryFixture(id = 7, name = "Salary", kind = TransactionKind.INCOME)
        )
        categories.seed(categoryFixture(id = 8, name = "Archived", isArchived = true))
    }

    @Test
    fun systemCategory_cannotBeDeleted() {
        assertThrows(DomainException.SystemRowNotDeletable::class.java) {
            runBlocking { deleteCategory(categoryId = 1) }
        }
        assertEquals("Food", categories.item(1)?.name)
    }

    @Test
    fun unusedUserCategory_isDeleted() = runBlocking {
        deleteCategory(categoryId = 2)

        assertNull(categories.item(2))
    }

    @Test
    fun usedCategory_withoutTarget_requiresReassignment() {
        categories.setUsage(3, 2)

        val error = assertThrows(DomainException.ReassignmentRequired::class.java) {
            runBlocking { deleteCategory(categoryId = 3) }
        }

        assertEquals(2, error.usageCount)
        assertEquals("Travel", categories.item(3)?.name)
        assertNull(categories.lastReassign)
    }

    @Test
    fun usedCategory_withWrongKindTarget_isRejected() {
        categories.setUsage(3, 1)

        assertThrows(DomainException.InvalidReassignment::class.java) {
            runBlocking { deleteCategory(categoryId = 3, reassignToId = 7) }
        }
        assertEquals("Travel", categories.item(3)?.name)
        assertNull(categories.lastReassign)
    }

    @Test
    fun usedCategory_withArchivedTarget_isRejected() {
        categories.setUsage(4, 1)

        assertThrows(DomainException.InvalidReassignment::class.java) {
            runBlocking { deleteCategory(categoryId = 4, reassignToId = 8) }
        }
        assertEquals("Gym", categories.item(4)?.name)
        assertNull(categories.lastReassign)
    }

    @Test
    fun usedCategory_withActiveTarget_reassignsAndDeletes() = runBlocking {
        categories.setUsage(5, 3)

        deleteCategory(categoryId = 5, reassignToId = 6)

        assertEquals(5L to 6L, categories.lastReassign)
        assertNull(categories.item(5))
        assertEquals(3, categories.item(6)?.let { categories.countUsage(6) })
        assertEquals(1, runner.callCount)
    }
}
