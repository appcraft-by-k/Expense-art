package com.appcraftbyk.expense.art.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.appcraftbyk.expense.art.R

/** Bottom navigation tabs, in display order. */
enum class AppTab(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    Home(R.string.tab_home, R.drawable.ic_tab_home),
    Expenses(R.string.tab_expenses, R.drawable.ic_tab_expenses),
    DebtGoals(R.string.tab_debt_goals, R.drawable.ic_tab_debt_goals),
    Reminders(R.string.tab_reminders, R.drawable.ic_tab_reminders)
}
