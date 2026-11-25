package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.core.time.DateRanges
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.HomeSummary
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.repository.BudgetRepository
import com.appcraftbyk.expense.art.domain.repository.DebtRepository
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import com.appcraftbyk.expense.art.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

/**
 * Aggregates everything the home dashboard shows for one month. All sums and grouping happen in
 * SQL; this class only combines flows. Deltas vs. the previous month stay in the home ViewModel.
 */
class ObserveHomeSummary @Inject constructor(
    private val transactions: TransactionRepository,
    private val debts: DebtRepository,
    private val savings: SavingsRepository,
    private val budgets: BudgetRepository,
    private val appClock: AppClock,
    private val dateRanges: DateRanges
) {

    operator fun invoke(yearMonth: YearMonth): Flow<HomeSummary> {
        val range = dateRanges.month(yearMonth)
        val today = appClock.todayEpochDay()

        val money = combine(
            transactions.observeTotalsByKind(range.fromMillis, range.toMillisExclusive),
            debts.observeOutstandingByDirection(),
            savings.observeTotalBalance(),
            savings.observeActiveGoalCount(),
            budgets.observe(yearMonth)
        ) { totals, outstanding, savingsTotal, activeGoals, budget ->
            MoneySnapshot(
                incomeMinor = totals[TransactionKind.INCOME] ?: 0L,
                expenseMinor = totals[TransactionKind.EXPENSE] ?: 0L,
                oweMinor = outstanding[DebtDirection.BORROWED] ?: 0L,
                owedToMeMinor = outstanding[DebtDirection.LENT] ?: 0L,
                totalSavingsMinor = savingsTotal,
                activeGoalCount = activeGoals,
                budgetLimitMinor = budget?.limitMinor
            )
        }

        return combine(
            money,
            transactions.observeSpendByCategory(
                TransactionKind.EXPENSE,
                range.fromMillis,
                range.toMillisExclusive,
                TOP_CATEGORY_LIMIT
            ),
            debts.observeUpcomingDue(today, today + UPCOMING_DUE_WINDOW_DAYS),
            transactions.observeRecent(RECENT_LIMIT)
        ) { snapshot, topCategories, upcomingDues, recent ->
            HomeSummary(
                period = yearMonth,
                incomeMinor = snapshot.incomeMinor,
                expenseMinor = snapshot.expenseMinor,
                oweMinor = snapshot.oweMinor,
                owedToMeMinor = snapshot.owedToMeMinor,
                totalSavingsMinor = snapshot.totalSavingsMinor,
                activeGoalCount = snapshot.activeGoalCount,
                budgetLimitMinor = snapshot.budgetLimitMinor,
                topCategories = topCategories,
                upcomingDues = upcomingDues,
                recentTransactions = recent
            )
        }
    }

    private data class MoneySnapshot(
        val incomeMinor: Long,
        val expenseMinor: Long,
        val oweMinor: Long,
        val owedToMeMinor: Long,
        val totalSavingsMinor: Long,
        val activeGoalCount: Int,
        val budgetLimitMinor: Long?
    )

    private companion object {
        const val TOP_CATEGORY_LIMIT = 4
        const val RECENT_LIMIT = 5
        const val UPCOMING_DUE_WINDOW_DAYS = 30L
    }
}
