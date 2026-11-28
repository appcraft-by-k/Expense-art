package com.appcraftbyk.expense.art.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.appcraftbyk.expense.art.R

/** Actions offered by the floating quick-add menu. */
enum class QuickAddAction(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    Expense(R.string.quick_add_expense, R.drawable.ic_tab_expenses),
    Income(R.string.quick_add_income, R.drawable.ic_income),
    Goal(R.string.quick_add_goal, R.drawable.ic_tab_debt_goals),
    Reminder(R.string.quick_add_reminder, R.drawable.ic_tab_reminders)
}
