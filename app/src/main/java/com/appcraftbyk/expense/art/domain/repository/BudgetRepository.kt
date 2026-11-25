package com.appcraftbyk.expense.art.domain.repository

import com.appcraftbyk.expense.art.domain.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {

    fun observe(yearMonth: YearMonth): Flow<MonthlyBudget?>

    suspend fun get(yearMonth: YearMonth): MonthlyBudget?

    /** Creates or replaces the single budget row for the month. */
    suspend fun set(yearMonth: YearMonth, limitMinor: Long)

    suspend fun clear(yearMonth: YearMonth)
}
