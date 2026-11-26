package com.appcraftbyk.expense.art.ui.expenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.common.PlaceholderScreen

/** Replaced by the real ledger in P5. */
@Composable
fun ExpensesPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_expenses),
        message = stringResource(R.string.placeholder_expenses),
        modifier = modifier
    )
}
