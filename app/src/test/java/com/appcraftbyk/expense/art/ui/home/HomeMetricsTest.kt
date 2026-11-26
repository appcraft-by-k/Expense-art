package com.appcraftbyk.expense.art.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeMetricsTest {

    @Test
    fun incomeDelta_usesPreviousMonthAsBaseline() {
        assertEquals(20, HomeMetrics.incomeDeltaPercent(currentMinor = 120_000, previousMinor = 100_000))
        assertEquals(-20, HomeMetrics.incomeDeltaPercent(currentMinor = 80_000, previousMinor = 100_000))
        assertEquals(0, HomeMetrics.incomeDeltaPercent(currentMinor = 100_000, previousMinor = 100_000))
        assertNull(HomeMetrics.incomeDeltaPercent(currentMinor = 100_000, previousMinor = 0))
    }

    @Test
    fun expenseShare_isNullWithoutIncome() {
        assertEquals(40, HomeMetrics.expenseShareOfIncomePercent(expenseMinor = 40_000, incomeMinor = 100_000))
        assertEquals(150, HomeMetrics.expenseShareOfIncomePercent(expenseMinor = 150_000, incomeMinor = 100_000))
        assertNull(HomeMetrics.expenseShareOfIncomePercent(expenseMinor = 40_000, incomeMinor = 0))
    }

    @Test
    fun budgetHealth_switchesAtEightyPercentAndOverBudget() {
        assertNull(HomeMetrics.budgetHealth(spentMinor = 10_000, limitMinor = null))
        assertNull(HomeMetrics.budgetHealth(spentMinor = 10_000, limitMinor = 0))

        assertEquals(
            BudgetHealth.ON_TRACK,
            HomeMetrics.budgetHealth(spentMinor = 79_999, limitMinor = 100_000)
        )
        assertEquals(
            BudgetHealth.NEAR_LIMIT,
            HomeMetrics.budgetHealth(spentMinor = 80_000, limitMinor = 100_000)
        )
        assertEquals(
            BudgetHealth.NEAR_LIMIT,
            HomeMetrics.budgetHealth(spentMinor = 100_000, limitMinor = 100_000)
        )
        assertEquals(
            BudgetHealth.OVER_BUDGET,
            HomeMetrics.budgetHealth(spentMinor = 100_001, limitMinor = 100_000)
        )
    }

    @Test
    fun budgetUsedFraction_clampsToBarRange() {
        assertEquals(0.5f, HomeMetrics.budgetUsedFraction(50_000, 100_000))
        assertEquals(1f, HomeMetrics.budgetUsedFraction(150_000, 100_000))
        assertEquals(0f, HomeMetrics.budgetUsedFraction(-10, 100_000))
        assertEquals(0f, HomeMetrics.budgetUsedFraction(50_000, null))
    }
}
