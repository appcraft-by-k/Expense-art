package com.appcraftbyk.expense.art.ui.addentry

import androidx.annotation.StringRes
import com.appcraftbyk.expense.art.R

/** The four global Add Entry modes (Plan §1). Reminders adds a fifth mode in v2. */
enum class AddEntryMode(@param:StringRes val labelRes: Int) {
    EXPENSE(R.string.add_entry_mode_expense),
    INCOME(R.string.add_entry_mode_income),
    DEBT_LEND(R.string.add_entry_mode_debt_lend),
    SAVINGS(R.string.add_entry_mode_savings)
}

/** Open state of the global Add Entry bottom sheet; owned by the activity-scoped ViewModel. */
data class AddEntrySheetState(
    val isOpen: Boolean = false,
    val presetMode: AddEntryMode = AddEntryMode.EXPENSE
)
