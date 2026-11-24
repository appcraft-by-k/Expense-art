package com.appcraftbyk.expense.art.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.appcraftbyk.expense.art.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth")
    fun observe(yearMonth: Int): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth")
    suspend fun get(yearMonth: Int): MonthlyBudgetEntity?

    @Upsert
    suspend fun upsert(budget: MonthlyBudgetEntity)

    @Query("DELETE FROM monthly_budgets WHERE yearMonth = :yearMonth")
    suspend fun delete(yearMonth: Int)
}
