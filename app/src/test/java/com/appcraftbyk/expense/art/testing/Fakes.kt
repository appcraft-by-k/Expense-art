package com.appcraftbyk.expense.art.testing

import com.appcraftbyk.expense.art.domain.model.Category
import com.appcraftbyk.expense.art.domain.model.CategorySpendItem
import com.appcraftbyk.expense.art.domain.model.Debt
import com.appcraftbyk.expense.art.domain.model.DebtContact
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtListFilter
import com.appcraftbyk.expense.art.domain.model.DebtRepayment
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.model.DebtWithContact
import com.appcraftbyk.expense.art.domain.model.MonthlyBudget
import com.appcraftbyk.expense.art.domain.model.NewCategory
import com.appcraftbyk.expense.art.domain.model.NewPaymentMethod
import com.appcraftbyk.expense.art.domain.model.NewSavingsPot
import com.appcraftbyk.expense.art.domain.model.NewSavingsType
import com.appcraftbyk.expense.art.domain.model.PaymentMethod
import com.appcraftbyk.expense.art.domain.model.SavingsEntry
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.model.SavingsPot
import com.appcraftbyk.expense.art.domain.model.SavingsType
import com.appcraftbyk.expense.art.domain.model.Transaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.model.UpcomingDueItem
import com.appcraftbyk.expense.art.domain.repository.BudgetRepository
import com.appcraftbyk.expense.art.domain.repository.CategoryRepository
import com.appcraftbyk.expense.art.domain.repository.DebtRepository
import com.appcraftbyk.expense.art.domain.repository.PaymentMethodRepository
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import com.appcraftbyk.expense.art.domain.repository.TransactionRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.YearMonth

/** In-memory fakes so use-case rules are testable on the JVM without Room or Hilt. */

class FakeTransactionRunner : TransactionRunner {
    var callCount = 0
        private set

    override suspend fun <T> inTransaction(block: suspend () -> T): T {
        callCount++
        return block()
    }
}

data class AddedTransaction(
    val kind: TransactionKind,
    val amountMinor: Long,
    val categoryId: Long?,
    val occurredAt: Long,
    val paymentMethodId: Long,
    val note: String?
)

data class LedgerQuery(
    val kind: TransactionKind,
    val fromMillis: Long,
    val toMillisExclusive: Long,
    val categoryId: Long?
)

class FakeTransactionRepository : TransactionRepository {
    var totals: Map<TransactionKind, Long> = emptyMap()
    var spendByCategory: List<CategorySpendItem> = emptyList()
    var recent: List<Transaction> = emptyList()
    var ledger: List<Transaction> = emptyList()
    val added = mutableListOf<AddedTransaction>()
    var lastLedgerQuery: LedgerQuery? = null

    override fun observeTotalsByKind(
        fromMillis: Long,
        toMillisExclusive: Long
    ): Flow<Map<TransactionKind, Long>> = flowOf(totals)

    override fun observeTotalByKind(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long
    ): Flow<Long> = flowOf(totals[kind] ?: 0L)

    override fun observeSpendByCategory(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long,
        limit: Int
    ): Flow<List<CategorySpendItem>> = flowOf(spendByCategory)

    override fun observeRange(
        kind: TransactionKind,
        fromMillis: Long,
        toMillisExclusive: Long,
        categoryId: Long?
    ): Flow<List<Transaction>> {
        lastLedgerQuery = LedgerQuery(kind, fromMillis, toMillisExclusive, categoryId)
        return flowOf(ledger)
    }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> = flowOf(recent)

    override suspend fun add(
        kind: TransactionKind,
        amountMinor: Long,
        categoryId: Long?,
        occurredAt: Long,
        paymentMethodId: Long,
        note: String?
    ): Long {
        added += AddedTransaction(kind, amountMinor, categoryId, occurredAt, paymentMethodId, note)
        return added.size.toLong()
    }
}

class FakeCategoryRepository : CategoryRepository {
    private val items = linkedMapOf<Long, Category>()
    private val usage = mutableMapOf<Long, Int>()
    private var nextId = 1L
    var lastReassign: Pair<Long, Long>? = null
        private set

    fun seed(category: Category) {
        items[category.id] = category
        nextId = maxOf(nextId, category.id + 1)
    }

    fun setUsage(categoryId: Long, count: Int) {
        usage[categoryId] = count
    }

    fun item(categoryId: Long): Category? = items[categoryId]

    fun all(): List<Category> = items.values.toList()

    override fun observeByKind(
        kind: TransactionKind,
        includeArchived: Boolean
    ): Flow<List<Category>> = flowOf(
        items.values.filter { it.kind == kind && (includeArchived || !it.isArchived) }
    )

    override fun observeAllByKind(kind: TransactionKind): Flow<List<Category>> =
        flowOf(items.values.filter { it.kind == kind })

    override fun observeAll(): Flow<List<Category>> = flowOf(items.values.toList())

    override suspend fun get(categoryId: Long): Category? = items[categoryId]

    override suspend fun findByName(
        kind: TransactionKind,
        name: String,
        excludeId: Long
    ): Category? = items.values.firstOrNull {
        it.kind == kind && it.id != excludeId && it.name.equals(name, ignoreCase = true)
    }

    override suspend fun countUsage(categoryId: Long): Int = usage[categoryId] ?: 0

    override suspend fun nextSortOrder(kind: TransactionKind): Int =
        (items.values.filter { it.kind == kind }.maxOfOrNull { it.sortOrder } ?: -1) + 1

    override suspend fun insert(category: NewCategory): Long {
        val id = nextId++
        items[id] = Category(
            id = id,
            uid = "uid-$id",
            name = category.name,
            kind = category.kind,
            iconKey = category.iconKey,
            sortOrder = category.sortOrder,
            isSystem = false,
            isArchived = false,
            createdAt = 0L
        )
        return id
    }

    override suspend fun update(category: Category) {
        items[category.id] = category
    }

    override suspend fun reassignTransactions(
        fromCategoryId: Long,
        toCategoryId: Long,
        updatedAt: Long
    ) {
        lastReassign = fromCategoryId to toCategoryId
        usage[toCategoryId] = (usage[toCategoryId] ?: 0) + (usage[fromCategoryId] ?: 0)
        usage[fromCategoryId] = 0
    }

    override suspend fun delete(categoryId: Long) {
        items.remove(categoryId)
    }
}

class FakePaymentMethodRepository : PaymentMethodRepository {
    private val items = linkedMapOf<Long, PaymentMethod>()
    private val usage = mutableMapOf<Long, Int>()
    private var nextId = 1L
    var lastReassign: Pair<Long, Long>? = null
        private set

    fun seed(paymentMethod: PaymentMethod) {
        items[paymentMethod.id] = paymentMethod
        nextId = maxOf(nextId, paymentMethod.id + 1)
    }

    fun setUsage(paymentMethodId: Long, count: Int) {
        usage[paymentMethodId] = count
    }

    fun item(paymentMethodId: Long): PaymentMethod? = items[paymentMethodId]

    fun all(): List<PaymentMethod> = items.values.toList()

    override fun observeAll(includeArchived: Boolean): Flow<List<PaymentMethod>> = flowOf(
        items.values.filter { includeArchived || !it.isArchived }
    )

    override suspend fun get(paymentMethodId: Long): PaymentMethod? = items[paymentMethodId]

    override suspend fun findByName(name: String, excludeId: Long): PaymentMethod? =
        items.values.firstOrNull { it.id != excludeId && it.name.equals(name, ignoreCase = true) }

    override suspend fun countUsage(paymentMethodId: Long): Int = usage[paymentMethodId] ?: 0

    override suspend fun nextSortOrder(): Int =
        (items.values.maxOfOrNull { it.sortOrder } ?: -1) + 1

    override suspend fun insert(paymentMethod: NewPaymentMethod): Long {
        val id = nextId++
        items[id] = PaymentMethod(
            id = id,
            uid = "uid-$id",
            name = paymentMethod.name,
            iconKey = paymentMethod.iconKey,
            sortOrder = paymentMethod.sortOrder,
            isSystem = false,
            isArchived = false,
            createdAt = 0L
        )
        return id
    }

    override suspend fun update(paymentMethod: PaymentMethod) {
        items[paymentMethod.id] = paymentMethod
    }

    override suspend fun reassignTransactions(
        fromPaymentMethodId: Long,
        toPaymentMethodId: Long,
        updatedAt: Long
    ) {
        lastReassign = fromPaymentMethodId to toPaymentMethodId
        usage[toPaymentMethodId] =
            (usage[toPaymentMethodId] ?: 0) + (usage[fromPaymentMethodId] ?: 0)
        usage[fromPaymentMethodId] = 0
    }

    override suspend fun delete(paymentMethodId: Long) {
        items.remove(paymentMethodId)
    }
}

class FakeBudgetRepository : BudgetRepository {
    private val budgets = mutableMapOf<YearMonth, MonthlyBudget>()

    override fun observe(yearMonth: YearMonth): Flow<MonthlyBudget?> =
        flowOf(budgets[yearMonth])

    override suspend fun get(yearMonth: YearMonth): MonthlyBudget? = budgets[yearMonth]

    override suspend fun set(yearMonth: YearMonth, limitMinor: Long) {
        budgets[yearMonth] = MonthlyBudget(yearMonth, limitMinor)
    }

    override suspend fun clear(yearMonth: YearMonth) {
        budgets.remove(yearMonth)
    }
}

class FakeDebtRepository : DebtRepository {
    private val debts = linkedMapOf<Long, Debt>()
    private val repayments = mutableListOf<DebtRepayment>()
    private var nextRepaymentId = 1L
    var upcomingDues: List<UpcomingDueItem> = emptyList()

    fun seed(debt: Debt) {
        debts[debt.id] = debt
    }

    fun seedRepayment(debtId: Long, amountMinor: Long, paidAt: Long = 0L) {
        repayments += DebtRepayment(
            id = nextRepaymentId++,
            uid = "repayment-${nextRepaymentId - 1}",
            debtId = debtId,
            amountMinor = amountMinor,
            paidAt = paidAt,
            note = null
        )
    }

    fun repaymentsFor(debtId: Long): List<DebtRepayment> =
        repayments.filter { it.debtId == debtId }

    private fun repaidTotal(debtId: Long): Long =
        repayments.filter { it.debtId == debtId }.sumOf { it.amountMinor }

    override fun observeOutstandingByDirection(): Flow<Map<DebtDirection, Long>> = flowOf(
        DebtDirection.entries.associateWith { direction ->
            debts.values
                .filter { it.direction == direction && it.status == DebtStatus.OPEN }
                .sumOf { (it.principalMinor - repaidTotal(it.id)).coerceAtLeast(0L) }
        }
    )

    override fun observeUpcomingDue(
        fromEpochDay: Long,
        toEpochDay: Long
    ): Flow<List<UpcomingDueItem>> = flowOf(upcomingDues)

    override fun observeDebts(
        filter: DebtListFilter,
        todayEpochDay: Long
    ): Flow<List<DebtWithContact>> = flowOf(emptyList())

    override fun observeDebtWithContact(debtId: Long): Flow<DebtWithContact?> = flowOf(null)

    override suspend fun getDebt(debtId: Long): Debt? = debts[debtId]

    override suspend fun outstandingMinor(debtId: Long): Long? =
        debts[debtId]?.let { (it.principalMinor - repaidTotal(debtId)).coerceAtLeast(0L) }

    override fun observeRepayments(debtId: Long): Flow<List<DebtRepayment>> =
        flowOf(repaymentsFor(debtId))

    override suspend fun insertRepayment(
        debtId: Long,
        amountMinor: Long,
        paidAt: Long,
        note: String?
    ): Long {
        val id = nextRepaymentId++
        repayments += DebtRepayment(
            id = id,
            uid = "repayment-$id",
            debtId = debtId,
            amountMinor = amountMinor,
            paidAt = paidAt,
            note = note
        )
        return id
    }

    override suspend fun markSettled(debtId: Long, settledAt: Long, updatedAt: Long) {
        debts[debtId] = debts.getValue(debtId).copy(
            status = DebtStatus.SETTLED,
            settledAt = settledAt,
            updatedAt = updatedAt
        )
    }

    override fun observeContacts(): Flow<List<DebtContact>> = flowOf(emptyList())
}

class FakeSavingsRepository : SavingsRepository {
    private val goals = linkedMapOf<Long, SavingsPot>()
    private val entries = mutableListOf<SavingsEntry>()
    private val types = linkedMapOf<Long, SavingsType>()
    private var nextGoalId = 1L
    private var nextEntryId = 1L
    private var nextTypeId = 1L
    var lastReassignedType: Pair<Long, Long>? = null
        private set

    fun seedType(type: SavingsType) {
        types[type.id] = type
        nextTypeId = maxOf(nextTypeId, type.id + 1)
    }

    fun seedPot(pot: SavingsPot) {
        goals[pot.id] = pot
        nextGoalId = maxOf(nextGoalId, pot.id + 1)
    }

    fun entriesFor(goalId: Long): List<SavingsEntry> =
        entries.filter { it.goalId == goalId }

    fun pot(goalId: Long): SavingsPot? = goals[goalId]

    fun type(typeId: Long): SavingsType? = types[typeId]

    private fun balance(goalId: Long): Long = entries
        .filter { it.goalId == goalId }
        .sumOf { entry ->
            when (entry.kind) {
                SavingsEntryKind.CONTRIBUTION -> entry.amountMinor
                SavingsEntryKind.WITHDRAWAL -> -entry.amountMinor
            }
        }

    override fun observePots(includeArchived: Boolean): Flow<List<SavingsPot>> = flowOf(
        goals.values
            .filter { includeArchived || !it.isArchived }
            .map { it.copy(balanceMinor = balance(it.id)) }
    )

    override fun observePot(goalId: Long): Flow<SavingsPot?> =
        flowOf(goals[goalId]?.copy(balanceMinor = balance(goalId)))

    override fun observeEntries(goalId: Long): Flow<List<SavingsEntry>> =
        flowOf(entriesFor(goalId))

    override suspend fun getPot(goalId: Long): SavingsPot? =
        goals[goalId]?.copy(balanceMinor = balance(goalId))

    override suspend fun potBalanceMinor(goalId: Long): Long = balance(goalId)

    override fun observeTotalBalance(): Flow<Long> = flowOf(
        goals.values.filter { !it.isArchived }.sumOf { balance(it.id) }
    )

    override fun observeActiveGoalCount(): Flow<Int> =
        flowOf(goals.values.count { !it.isArchived })

    override suspend fun nextGoalSortOrder(): Int =
        (goals.values.maxOfOrNull { it.sortOrder } ?: -1) + 1

    override suspend fun insertGoal(pot: NewSavingsPot): Long {
        val id = nextGoalId++
        goals[id] = SavingsPot(
            id = id,
            uid = "pot-$id",
            name = pot.name,
            typeId = pot.typeId,
            typeName = types[pot.typeId]?.name ?: "",
            institution = pot.institution,
            targetMinor = pot.targetMinor,
            deadline = pot.deadline,
            balanceMinor = 0L,
            isArchived = false,
            sortOrder = pot.sortOrder,
            createdAt = 0L
        )
        return id
    }

    override suspend fun updateGoal(pot: SavingsPot) {
        goals[pot.id] = pot
    }

    override suspend fun insertEntry(
        goalId: Long,
        kind: SavingsEntryKind,
        amountMinor: Long,
        occurredAt: Long,
        note: String?
    ): Long {
        val id = nextEntryId++
        entries += SavingsEntry(
            id = id,
            uid = "entry-$id",
            goalId = goalId,
            kind = kind,
            amountMinor = amountMinor,
            occurredAt = occurredAt,
            note = note
        )
        return id
    }

    override fun observeTypes(includeArchived: Boolean): Flow<List<SavingsType>> = flowOf(
        types.values.filter { includeArchived || !it.isArchived }
    )

    override suspend fun getType(typeId: Long): SavingsType? = types[typeId]

    override suspend fun findTypeByName(name: String, excludeId: Long): SavingsType? =
        types.values.firstOrNull { it.id != excludeId && it.name.equals(name, ignoreCase = true) }

    override suspend fun countTypeUsage(typeId: Long): Int =
        goals.values.count { it.typeId == typeId }

    override suspend fun nextTypeSortOrder(): Int =
        (types.values.maxOfOrNull { it.sortOrder } ?: -1) + 1

    override suspend fun insertType(type: NewSavingsType): Long {
        val id = nextTypeId++
        types[id] = SavingsType(
            id = id,
            uid = "type-$id",
            name = type.name,
            iconKey = type.iconKey,
            isSystem = false,
            isArchived = false,
            sortOrder = type.sortOrder,
            createdAt = 0L
        )
        return id
    }

    override suspend fun updateType(type: SavingsType) {
        types[type.id] = type
    }

    override suspend fun reassignGoals(fromTypeId: Long, toTypeId: Long) {
        lastReassignedType = fromTypeId to toTypeId
        goals.replaceAll { _, pot ->
            if (pot.typeId == fromTypeId) {
                pot.copy(typeId = toTypeId, typeName = types[toTypeId]?.name ?: pot.typeName)
            } else {
                pot
            }
        }
    }

    override suspend fun deleteType(typeId: Long) {
        types.remove(typeId)
    }
}
