package com.appcraftbyk.expense.art.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appcraftbyk.expense.art.data.local.entity.CategoryEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtContactEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtRepaymentEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsEntryEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsGoalEntity
import com.appcraftbyk.expense.art.data.local.entity.TransactionEntity
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtListFilter
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    // Test-only: instrumented tests run on a dedicated thread; production code never enables this.
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        DatabaseSeeder.seed(db)
                    }
                }
            )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun seeding_insertsSystemReferenceData_andIsIdempotent() = runBlocking {
        val categories = db.categoryDao().observeAll().first()
        assertEquals(14, categories.size)
        assertEquals(9, categories.count { it.kind == TransactionKind.EXPENSE })
        assertEquals(5, categories.count { it.kind == TransactionKind.INCOME })
        assertTrue(categories.all { it.isSystem })

        val types = db.savingsDao().observeTypes(includeArchived = true).first()
        assertEquals(7, types.size)

        val methods = db.paymentMethodDao().observeAll(includeArchived = true).first()
        assertEquals(5, methods.size)
        assertTrue(methods.all { it.isSystem })

        // Re-running the seeder must not duplicate rows or change fixed IDs.
        DatabaseSeeder.seed(db.openHelper.writableDatabase)
        assertEquals(14, db.categoryDao().observeAll().first().size)
        assertEquals(7, db.savingsDao().observeTypes(includeArchived = true).first().size)
        assertEquals(5, db.paymentMethodDao().observeAll(includeArchived = true).first().size)
        assertNotNull(db.categoryDao().getById(1))
    }

    @Test
    fun seededRows_haveDeterministicUids_forCloudMerge() = runBlocking {
        // Same uid on every install/device, so system rows merge instead of duplicating.
        assertEquals("sys-category-expense-food", db.categoryDao().getById(1)!!.uid)
        assertEquals("sys-category-income-other", db.categoryDao().getById(14)!!.uid)
        assertEquals("sys-payment-method-upi", db.paymentMethodDao().getById(2)!!.uid)
        assertEquals("sys-savings-type-bank-fd", db.savingsDao().getType(1)!!.uid)
    }

    @Test
    fun uidIsUnique_perTable() = runBlocking {
        val categoryDao = db.categoryDao()
        categoryDao.insert(category(name = "Cafe", uid = "duplicate-uid"))
        var thrown: SQLiteConstraintException? = null
        try {
            categoryDao.insert(category(name = "Diner", uid = "duplicate-uid"))
        } catch (e: SQLiteConstraintException) {
            thrown = e
        }
        assertNotNull("Duplicate uids must be rejected", thrown)
    }

    @Test
    fun transactionAggregates_areComputedInSql() = runBlocking {
        val categoryDao = db.categoryDao()
        val transactionDao = db.transactionDao()

        val foodId = categoryDao.insert(
            category(name = "Cafe", kind = TransactionKind.EXPENSE, sortOrder = 0)
        )
        val groceriesId = categoryDao.insert(
            category(name = "Market", kind = TransactionKind.EXPENSE, sortOrder = 1)
        )

        val novemberStart = 1_793_471_400_000L // 2026-11-01T00:00:00+05:30
        transactionDao.insert(transaction(foodId, 25_000, novemberStart + 1_000))
        transactionDao.insert(transaction(foodId, 15_000, novemberStart + 2_000))
        transactionDao.insert(transaction(groceriesId, 40_000, novemberStart + 3_000))
        transactionDao.insert(
            transaction(
                categoryId = null,
                amountMinor = 500_000,
                occurredAt = novemberStart + 4_000,
                kind = TransactionKind.INCOME
            )
        )
        // Outside the queried window.
        transactionDao.insert(transaction(foodId, 99_000, novemberStart - 1))

        val monthEnd = novemberStart + 30L * 24 * 60 * 60 * 1000
        val totals = transactionDao.observeTotalsByKind(novemberStart, monthEnd).first()
            .associate { it.kind to it.totalMinor }
        assertEquals(80_000L, totals[TransactionKind.EXPENSE])
        assertEquals(500_000L, totals[TransactionKind.INCOME])

        val spend = transactionDao
            .observeSpendByCategory(TransactionKind.EXPENSE, novemberStart, monthEnd, limit = 4)
            .first()
        assertEquals(2, spend.size)
        assertEquals(groceriesId, spend[0].categoryId)
        assertEquals(40_000L, spend[0].totalMinor)
        assertEquals(foodId, spend[1].categoryId)
        assertEquals(40_000L, spend[1].totalMinor)

        val ledger = transactionDao
            .observeRange(TransactionKind.EXPENSE, novemberStart, monthEnd, categoryId = null)
            .first()
        assertEquals(3, ledger.size)
        assertEquals("UPI", ledger.first().paymentMethodName)
    }

    @Test
    fun deletingInUseCategory_isRestricted() = runBlocking {
        val category = db.categoryDao().getById(1)!!
        db.transactionDao().insert(transaction(category.id, 10_000, 1_793_494_800_000L))

        var thrown: SQLiteConstraintException? = null
        try {
            db.categoryDao().delete(category)
        } catch (e: SQLiteConstraintException) {
            thrown = e
        }
        assertNotNull("FK RESTRICT must block deleting a category with transactions", thrown)
        assertNotNull(db.categoryDao().getById(1))
    }

    @Test
    fun deletingInUsePaymentMethod_isRestricted() = runBlocking {
        val methodDao = db.paymentMethodDao()
        val upi = methodDao.getById(2)!!
        db.transactionDao().insert(
            transaction(categoryId = 1, amountMinor = 10_000, occurredAt = 1L, paymentMethodId = upi.id)
        )

        var thrown: SQLiteConstraintException? = null
        try {
            methodDao.delete(upi)
        } catch (e: SQLiteConstraintException) {
            thrown = e
        }
        assertNotNull("FK RESTRICT must block deleting a payment method in use", thrown)

        // Reassigning first makes the delete legal; the repository wraps both in one transaction.
        methodDao.reassignTransactions(fromPaymentMethodId = upi.id, toPaymentMethodId = 1, updatedAt = 2L)
        methodDao.delete(upi)
        assertEquals(0, methodDao.countUsage(upi.id))
    }

    @Test
    fun debtOutstanding_isDerivedAndKeptSeparatePerDirection() = runBlocking {
        val debtDao = db.debtDao()
        val contactId = debtDao.insertContact(
            DebtContactEntity(name = "Asha", note = null, createdAt = 1L)
        )
        val borrowedId = debtDao.insertDebt(debt(contactId, DebtDirection.BORROWED, principal = 50_000))
        debtDao.insertDebt(debt(contactId, DebtDirection.LENT, principal = 100_000))
        debtDao.insertRepayment(
            DebtRepaymentEntity(debtId = borrowedId, amountMinor = 20_000, paidAt = 2L, note = null)
        )

        val outstanding = debtDao.observeOutstandingByDirection().first()
            .associate { it.direction to it.outstandingMinor }
        assertEquals(30_000L, outstanding[DebtDirection.BORROWED])
        assertEquals(100_000L, outstanding[DebtDirection.LENT])
        assertEquals(30_000L, debtDao.outstandingMinor(borrowedId))
    }

    @Test
    fun debtFilters_overdueAndSettled_areResolvedInSql() = runBlocking {
        val debtDao = db.debtDao()
        val contactId = debtDao.insertContact(
            DebtContactEntity(name = "Ravi", note = null, createdAt = 1L)
        )
        val today = 20_000L
        debtDao.insertDebt(
            debt(contactId, DebtDirection.BORROWED, principal = 10_000, dueDate = today - 3)
        )
        debtDao.insertDebt(
            debt(contactId, DebtDirection.LENT, principal = 5_000, dueDate = today + 5)
        )
        debtDao.insertDebt(
            debt(
                contactId,
                DebtDirection.LENT,
                principal = 7_000,
                dueDate = today + 2,
                status = DebtStatus.SETTLED
            )
        )

        assertEquals(3, debtDao.observeDebtsWithContact(DebtListFilter.ALL, today).first().size)
        assertEquals(1, debtDao.observeDebtsWithContact(DebtListFilter.OVERDUE, today).first().size)
        assertEquals(1, debtDao.observeDebtsWithContact(DebtListFilter.SETTLED, today).first().size)
        assertEquals(1, debtDao.observeDebtsWithContact(DebtListFilter.I_OWE, today).first().size)
        assertEquals(2, debtDao.observeDebtsWithContact(DebtListFilter.OWED_TO_ME, today).first().size)

        // Settled debts drop out of upcoming dues even when their due date is in range.
        val upcoming = debtDao.observeUpcomingDue(today, today + 7).first()
        assertEquals(1, upcoming.size)
        assertEquals(5_000L, upcoming[0].dueMinor)
    }

    @Test
    fun savingsPotBalance_isContributionsMinusWithdrawals() = runBlocking {
        val savingsDao = db.savingsDao()
        val goalId = savingsDao.insertGoal(
            SavingsGoalEntity(
                name = "Emergency Fund",
                typeId = 1,
                institution = "HDFC",
                targetMinor = 500_000,
                deadline = 20_400,
                sortOrder = 0,
                createdAt = 1L
            )
        )
        savingsDao.insertEntry(
            SavingsEntryEntity(
                goalId = goalId,
                kind = SavingsEntryKind.CONTRIBUTION,
                amountMinor = 100_000,
                occurredAt = 10L,
                note = null
            )
        )
        savingsDao.insertEntry(
            SavingsEntryEntity(
                goalId = goalId,
                kind = SavingsEntryKind.WITHDRAWAL,
                amountMinor = 25_000,
                occurredAt = 20L,
                note = null
            )
        )

        val pot = savingsDao.observePot(goalId).first()!!
        assertEquals(75_000L, pot.balanceMinor)
        assertEquals("Bank FD", pot.typeName)
        assertEquals(75_000L, savingsDao.potBalanceMinor(goalId))
        assertEquals(75_000L, savingsDao.observeTotalBalance().first())
        assertEquals(1, savingsDao.observeActiveGoalCount().first())
    }

    @Test
    fun deletingDebt_cascadesRepayments() = runBlocking {
        val debtDao = db.debtDao()
        val contactId = debtDao.insertContact(
            DebtContactEntity(name = "Meera", note = null, createdAt = 1L)
        )
        val debtId = debtDao.insertDebt(debt(contactId, DebtDirection.LENT, principal = 30_000))
        debtDao.insertRepayment(
            DebtRepaymentEntity(debtId = debtId, amountMinor = 10_000, paidAt = 2L, note = null)
        )
        assertEquals(1, debtDao.observeRepayments(debtId).first().size)

        db.openHelper.writableDatabase.execSQL("DELETE FROM debts WHERE id = $debtId")
        assertEquals(0, debtDao.observeRepayments(debtId).first().size)
    }

    private fun transaction(
        categoryId: Long?,
        amountMinor: Long,
        occurredAt: Long,
        kind: TransactionKind = TransactionKind.EXPENSE,
        paymentMethodId: Long = 2
    ) = TransactionEntity(
        kind = kind,
        amountMinor = amountMinor,
        categoryId = categoryId,
        occurredAt = occurredAt,
        paymentMethodId = paymentMethodId,
        note = null,
        createdAt = occurredAt,
        updatedAt = occurredAt
    )

    private fun debt(
        contactId: Long,
        direction: DebtDirection,
        principal: Long,
        dueDate: Long? = null,
        status: DebtStatus = DebtStatus.OPEN
    ) = DebtEntity(
        contactId = contactId,
        direction = direction,
        principalMinor = principal,
        startDate = 19_000,
        dueDate = dueDate,
        note = null,
        status = status,
        settledAt = null,
        createdAt = 1L,
        updatedAt = 1L
    )

    private fun category(
        name: String,
        kind: TransactionKind = TransactionKind.EXPENSE,
        sortOrder: Int = 0,
        uid: String = com.appcraftbyk.expense.art.core.id.Uids.new()
    ) = CategoryEntity(
        uid = uid,
        name = name,
        kind = kind,
        iconKey = "other",
        sortOrder = sortOrder,
        isSystem = false,
        isArchived = false,
        createdAt = 1L
    )
}
