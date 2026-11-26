package com.appcraftbyk.expense.art.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.addentry.AddEntryMode

object Routes {
    const val HOME = "home"
    const val EXPENSES = "expenses"
    const val DEBT_GOALS = "debt_goals"
    const val REMINDERS = "reminders"
    const val SETTINGS = "settings"
}

/**
 * The four bottom-bar destinations (Plan §11). [addEntryMode] is the FAB preset for the tab;
 * `null` hides the FAB (Reminders, v2).
 */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val addEntryMode: AddEntryMode?
) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Filled.Home, AddEntryMode.EXPENSE),
    EXPENSES(
        Routes.EXPENSES,
        R.string.nav_expenses,
        Icons.AutoMirrored.Filled.List,
        AddEntryMode.EXPENSE
    ),
    DEBT_GOALS(
        Routes.DEBT_GOALS,
        R.string.nav_debt_goals,
        Icons.Filled.AccountBox,
        AddEntryMode.DEBT_LEND
    ),
    REMINDERS(Routes.REMINDERS, R.string.nav_reminders, Icons.Filled.Notifications, null)
}

/** Switches tabs while preserving each tab's back stack (Plan §11). */
fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
