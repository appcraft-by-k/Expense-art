package com.appcraftbyk.expense.art.ui.debt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.common.PlaceholderScreen

/** Replaced by the debt and savings screens in P6. */
@Composable
fun DebtGoalsPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_debt_goals),
        message = stringResource(R.string.placeholder_debt_goals),
        modifier = modifier
    )
}
