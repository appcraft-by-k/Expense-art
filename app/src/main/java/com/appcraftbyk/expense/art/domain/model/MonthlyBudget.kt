package com.appcraftbyk.expense.art.domain.model

import java.time.YearMonth

data class MonthlyBudget(
    val yearMonth: YearMonth,
    val limitMinor: Long
)
