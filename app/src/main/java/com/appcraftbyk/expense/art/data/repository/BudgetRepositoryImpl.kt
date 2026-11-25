package com.appcraftbyk.expense.art.data.repository

import com.appcraftbyk.expense.art.core.id.Uids
import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.core.time.budgetKeyToYearMonth
import com.appcraftbyk.expense.art.core.time.toBudgetKey
import com.appcraftbyk.expense.art.data.local.dao.BudgetDao
import com.appcraftbyk.expense.art.data.local.entity.MonthlyBudgetEntity
import com.appcraftbyk.expense.art.domain.model.MonthlyBudget
import com.appcraftbyk.expense.art.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
    private val appClock: AppClock
) : BudgetRepository {

    override fun observe(yearMonth: YearMonth): Flow<MonthlyBudget?> =
        dao.observe(yearMonth.toBudgetKey()).map { entity -> entity?.toDomain() }

    override suspend fun get(yearMonth: YearMonth): MonthlyBudget? =
        dao.get(yearMonth.toBudgetKey())?.toDomain()

    override suspend fun set(yearMonth: YearMonth, limitMinor: Long) {
        val key = yearMonth.toBudgetKey()
        val existing = dao.get(key)
        dao.upsert(
            MonthlyBudgetEntity(
                id = existing?.id ?: 0,
                uid = existing?.uid ?: Uids.new(),
                yearMonth = key,
                limitMinor = limitMinor,
                createdAt = existing?.createdAt ?: appClock.nowMillis()
            )
        )
    }

    override suspend fun clear(yearMonth: YearMonth) = dao.delete(yearMonth.toBudgetKey())

    private fun MonthlyBudgetEntity.toDomain() = MonthlyBudget(
        yearMonth = budgetKeyToYearMonth(yearMonth),
        limitMinor = limitMinor
    )
}
