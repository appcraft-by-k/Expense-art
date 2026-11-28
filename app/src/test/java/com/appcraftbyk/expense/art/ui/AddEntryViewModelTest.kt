package com.appcraftbyk.expense.art.ui

import com.appcraftbyk.expense.art.data.CategoryRepository
import com.appcraftbyk.expense.art.data.PaymentMethodRepository
import com.appcraftbyk.expense.art.data.TransactionRepository
import com.appcraftbyk.expense.art.data.local.CategoryDao
import com.appcraftbyk.expense.art.data.local.CategoryEntity
import com.appcraftbyk.expense.art.data.local.PaymentMethodDao
import com.appcraftbyk.expense.art.data.local.PaymentMethodEntity
import com.appcraftbyk.expense.art.data.local.TransactionDao
import com.appcraftbyk.expense.art.data.local.TransactionEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEntryViewModelTest {

    private val fixedNow = 1_700_000_000_000L
    private val testDispatcher = StandardTestDispatcher()

    private val expenseCategories = listOf(
        CategoryEntity(id = 1, name = "Food & Dining", type = TransactionType.EXPENSE),
        CategoryEntity(id = 2, name = "Travel", type = TransactionType.EXPENSE)
    )
    private val incomeCategories = listOf(
        CategoryEntity(id = 3, name = "Salary", type = TransactionType.INCOME)
    )
    private val paymentMethods = listOf(
        PaymentMethodEntity(id = 10, name = "UPI"),
        PaymentMethodEntity(id = 11, name = "Cash")
    )

    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var viewModel: AddEntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionDao = FakeTransactionDao()
        viewModel = AddEntryViewModel(
            transactionRepository = TransactionRepository(transactionDao, now = { fixedNow }),
            categoryRepository = CategoryRepository(
                FakeCategoryDao(expenseCategories, incomeCategories),
                now = { fixedNow }
            ),
            paymentMethodRepository = PaymentMethodRepository(
                FakePaymentMethodDao(paymentMethods),
                now = { fixedNow }
            ),
            now = { fixedNow }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start loads catalogs and selects defaults`() = runTest {
        viewModel.start(TransactionType.EXPENSE)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals(expenseCategories, state.categories)
        assertEquals(1L, state.selectedCategoryId)
        assertEquals(paymentMethods, state.paymentMethods)
        assertEquals(10L, state.selectedPaymentMethodId)
        assertTrue(state.isCategoriesLoaded)
        assertTrue(state.isPaymentMethodsLoaded)
        assertFalse(state.canSave)
    }

    @Test
    fun `saving an expense stores the entry`() = runTest {
        viewModel.start(TransactionType.EXPENSE)
        advanceUntilIdle()

        viewModel.onAmountChanged("1450.50")
        viewModel.onNoteChanged("Dinner with team")
        viewModel.save()
        advanceUntilIdle()

        val saved = transactionDao.inserted.single()
        assertEquals(TransactionType.EXPENSE, saved.type)
        assertEquals(145_050L, saved.amountMinor)
        assertEquals(1L, saved.categoryId)
        assertEquals(10L, saved.paymentMethodId)
        assertEquals("Dinner with team", saved.note)
        assertEquals(fixedNow, saved.occurredAt)
        assertEquals(1L, viewModel.savedEntryId.value)
        assertEquals("", viewModel.uiState.value.amountInput)
    }

    @Test
    fun `switching to income loads income categories and saves income`() = runTest {
        viewModel.start(TransactionType.EXPENSE)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.INCOME)
        advanceUntilIdle()
        assertEquals(incomeCategories, viewModel.uiState.value.categories)
        assertEquals(3L, viewModel.uiState.value.selectedCategoryId)

        viewModel.onAmountChanged("2500")
        viewModel.save()
        advanceUntilIdle()

        val saved = transactionDao.inserted.single()
        assertEquals(TransactionType.INCOME, saved.type)
        assertEquals(250_000L, saved.amountMinor)
        assertEquals(3L, saved.categoryId)
    }

    @Test
    fun `saving without an amount shows an error and inserts nothing`() = runTest {
        viewModel.start(TransactionType.EXPENSE)
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.amountError)
        assertTrue(transactionDao.inserted.isEmpty())
    }

    @Test
    fun `quick amount chips add to the current amount`() = runTest {
        viewModel.start(TransactionType.EXPENSE)
        advanceUntilIdle()

        viewModel.onAmountChanged("1450")
        viewModel.onQuickAmountAdded(10_000)

        assertEquals("1550", viewModel.uiState.value.amountInput)
        assertEquals(155_000L, viewModel.uiState.value.amountMinor)

        viewModel.onClearAmount()
        assertEquals("", viewModel.uiState.value.amountInput)
        assertEquals(0L, viewModel.uiState.value.amountMinor)
    }

    @Test
    fun `amount input keeps two decimal places and nine digits`() {
        assertEquals("12.34", sanitizeAmountInput("12.345"))
        assertEquals("12.34", sanitizeAmountInput("12.3.4"))
        assertEquals("", sanitizeAmountInput("abc"))
        assertEquals("123456789", sanitizeAmountInput("1234567890"))
        assertEquals("1450.00", sanitizeAmountInput("1,450.00"))
    }

    @Test
    fun `amount parsing and formatting use minor units`() {
        assertEquals(145_050L, parseAmountMinor("1450.5"))
        assertEquals(145_000L, parseAmountMinor("1450"))
        assertEquals(0L, parseAmountMinor(""))
        assertEquals("1450.50", formatAmountInput(145_050L))
        assertEquals("1450", formatAmountInput(145_000L))
    }

    private class FakeTransactionDao : TransactionDao {
        val inserted = mutableListOf<TransactionEntity>()

        override suspend fun insert(transaction: TransactionEntity): Long {
            val stored = transaction.copy(id = inserted.size + 1L)
            inserted += stored
            return stored.id
        }

        override suspend fun update(transaction: TransactionEntity) = Unit

        override suspend fun delete(transaction: TransactionEntity) = Unit

        override fun observeAll(): Flow<List<TransactionEntity>> = flowOf(inserted)

        override fun observeByType(type: TransactionType): Flow<List<TransactionEntity>> =
            flowOf(inserted.filter { it.type == type })

        override suspend fun findById(id: Long): TransactionEntity? =
            inserted.find { it.id == id }

        override fun observeTotal(type: TransactionType, from: Long, until: Long): Flow<Long> =
            flowOf(0L)
    }

    private class FakeCategoryDao(
        private val expenseCategories: List<CategoryEntity>,
        private val incomeCategories: List<CategoryEntity>
    ) : CategoryDao {

        override suspend fun insert(category: CategoryEntity): Long = 0

        override suspend fun update(category: CategoryEntity) = Unit

        override suspend fun delete(category: CategoryEntity) = Unit

        override fun observeAll(): Flow<List<CategoryEntity>> =
            flowOf(expenseCategories + incomeCategories)

        override fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> =
            flowOf(
                if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
            )

        override suspend fun findById(id: Long): CategoryEntity? =
            (expenseCategories + incomeCategories).find { it.id == id }

        override suspend fun findByName(name: String, type: TransactionType): CategoryEntity? =
            null

        override suspend fun countTransactions(categoryId: Long): Int = 0
    }

    private class FakePaymentMethodDao(
        private val paymentMethods: List<PaymentMethodEntity>
    ) : PaymentMethodDao {

        override suspend fun insert(paymentMethod: PaymentMethodEntity): Long = 0

        override suspend fun update(paymentMethod: PaymentMethodEntity) = Unit

        override suspend fun delete(paymentMethod: PaymentMethodEntity) = Unit

        override fun observeAll(): Flow<List<PaymentMethodEntity>> = flowOf(paymentMethods)

        override suspend fun findById(id: Long): PaymentMethodEntity? =
            paymentMethods.find { it.id == id }

        override suspend fun findByName(name: String): PaymentMethodEntity? = null
    }
}
