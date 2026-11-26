package com.appcraftbyk.expense.art.ui.home

import kotlin.math.roundToInt

/** How the current month's spending compares with its budget. */
enum class BudgetHealth { ON_TRACK, NEAR_LIMIT, OVER_BUDGET }

/**
 * Pure dashboard math (Plan §12: deltas and percentages are ViewModel math, not SQL).
 * Kept separate from the ViewModel so the thresholds are unit-testable on the JVM.
 */
object HomeMetrics {

    /** Spending at or above this share of the budget is flagged as near the limit. */
    private const val NEAR_LIMIT_FRACTION = 0.8

    /** Percent change in income vs. the previous month; `null` when there is no baseline. */
    fun incomeDeltaPercent(currentMinor: Long, previousMinor: Long): Int? =
        if (previousMinor <= 0L) {
            null
        } else {
            (((currentMinor - previousMinor) * 100.0) / previousMinor).roundToInt()
        }

    /** Share of this month's income that was spent; `null` when no income was recorded. */
    fun expenseShareOfIncomePercent(expenseMinor: Long, incomeMinor: Long): Int? =
        if (incomeMinor <= 0L) {
            null
        } else {
            ((expenseMinor * 100.0) / incomeMinor).roundToInt()
        }

    fun budgetHealth(spentMinor: Long, limitMinor: Long?): BudgetHealth? {
        if (limitMinor == null || limitMinor <= 0L) return null
        return when {
            spentMinor > limitMinor -> BudgetHealth.OVER_BUDGET
            spentMinor >= limitMinor * NEAR_LIMIT_FRACTION -> BudgetHealth.NEAR_LIMIT
            else -> BudgetHealth.ON_TRACK
        }
    }

    /** Fraction of the budget used, clamped to `0f..1f` for the progress bar. */
    fun budgetUsedFraction(spentMinor: Long, limitMinor: Long?): Float {
        if (limitMinor == null || limitMinor <= 0L) return 0f
        return (spentMinor.toDouble() / limitMinor).coerceIn(0.0, 1.0).toFloat()
    }
}
