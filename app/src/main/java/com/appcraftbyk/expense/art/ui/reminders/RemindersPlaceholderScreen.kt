package com.appcraftbyk.expense.art.ui.reminders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.common.PlaceholderScreen

/** v1 keeps this tab as an explicit "coming soon" placeholder (Plan D8). */
@Composable
fun RemindersPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.nav_reminders),
        message = stringResource(R.string.placeholder_reminders),
        modifier = modifier
    )
}
