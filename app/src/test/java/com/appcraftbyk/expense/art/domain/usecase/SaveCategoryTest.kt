package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.testing.FakeCategoryRepository
import com.appcraftbyk.expense.art.testing.categoryFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveCategoryTest {

    private val categories = FakeCategoryRepository()
    private val saveCategory = SaveCategory(categories)

    @Before
    fun setUp() {
        categories.seed(
            categoryFixture(id = 1, name = "Food", isSystem = true, sortOrder = 0, uid = "sys-food")
        )
    }

    @Test
    fun create_usesNextSortOrderAndIsNotSystem() = runBlocking {
        saveCategory(categoryId = null, kind = TransactionKind.EXPENSE, name = "Cafe", iconKey = "cafe")

        val created = categories.item(2)!!
        assertEquals("Cafe", created.name)
        assertEquals(1, created.sortOrder)
        assertEquals(false, created.isSystem)
        assertTrue(created.uid.isNotBlank())
    }

    @Test
    fun duplicateName_isRejectedCaseInsensitively() {
        assertThrows(DomainException.DuplicateName::class.java) {
            runBlocking {
                saveCategory(
                    categoryId = null,
                    kind = TransactionKind.EXPENSE,
                    name = "  food  ",
                    iconKey = "food"
                )
            }
        }
        assertEquals(1, categories.all().size)
    }

    @Test
    fun blankName_isRejected() {
        assertThrows(DomainException.BlankName::class.java) {
            runBlocking {
                saveCategory(
                    categoryId = null,
                    kind = TransactionKind.EXPENSE,
                    name = "   ",
                    iconKey = "other"
                )
            }
        }
    }

    @Test
    fun edit_preservesUidSystemFlagAndCreatedAt() = runBlocking {
        saveCategory(
            categoryId = 1,
            kind = TransactionKind.EXPENSE,
            name = "Dining",
            iconKey = "dining",
            isArchived = true
        )

        val updated = categories.item(1)!!
        assertEquals("Dining", updated.name)
        assertEquals("sys-food", updated.uid)
        assertEquals(true, updated.isSystem)
        assertEquals(true, updated.isArchived)
        assertEquals(1L, updated.createdAt)
    }

    @Test
    fun kindCannotChangeOnEdit() {
        assertThrows(DomainException.CategoryKindMismatch::class.java) {
            runBlocking {
                saveCategory(
                    categoryId = 1,
                    kind = TransactionKind.INCOME,
                    name = "Food",
                    iconKey = "food"
                )
            }
        }
        assertEquals("Food", categories.item(1)?.name)
    }
}
