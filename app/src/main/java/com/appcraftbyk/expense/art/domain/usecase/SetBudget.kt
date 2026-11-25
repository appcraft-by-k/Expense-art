package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.repository.BudgetRepository
import java.time.YearMonth
import javax.inject.Inject

/** One budget per month. A null or zero limit clears it; negative limits are rejected. */
class SetBudget @Inject constructor(
    private val budgets: BudgetRepository
) {

    suspend operator fun invoke(yearMonth: YearMonth, limitMinor: Long?) {
        if (limitMinor == null || limitMinor == 0L) {
            budgets.clear(yearMonth)
            return
        }
        if (limitMinor < 0) throw DomainException.InvalidAmount
        budgets.set(yearMonth, limitMinor)
    }
}
