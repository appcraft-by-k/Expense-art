package com.appcraftbyk.expense.art.domain.model

import java.time.YearMonth

/** Everything the home dashboard needs for one month, already aggregated in SQL. */
data class HomeSummary(
    val period: YearMonth,
    val incomeMinor: Long,
    val expenseMinor: Long,
    /** Income of the month before [period]; drives the "vs last month" delta. */
    val previousIncomeMinor: Long,
    /** Never netted against [owedToMeMinor]. */
    val oweMinor: Long,
    val owedToMeMinor: Long,
    val totalSavingsMinor: Long,
    val activeGoalCount: Int,
    val budgetLimitMinor: Long?,
    val topCategories: List<CategorySpendItem>,
    val upcomingDues: List<UpcomingDueItem>,
    val recentTransactions: List<Transaction>
)
