package com.appcraftbyk.expense.art.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appcraftbyk.expense.art.ui.addentry.AddEntryMode
import com.appcraftbyk.expense.art.ui.debt.DebtGoalsPlaceholderScreen
import com.appcraftbyk.expense.art.ui.expenses.ExpensesPlaceholderScreen
import com.appcraftbyk.expense.art.ui.home.HomeScreen
import com.appcraftbyk.expense.art.ui.main.MainViewModel
import com.appcraftbyk.expense.art.ui.reminders.RemindersPlaceholderScreen
import com.appcraftbyk.expense.art.ui.settings.SettingsScreen

/** Four tab destinations plus the Settings detail route (not a bottom-bar tab). */
@Composable
fun ExpenseArtNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by mainViewModel.selectedYearMonth.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                selectedMonth = selectedMonth,
                onSelectMonth = mainViewModel::selectYearMonth,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onViewLedger = { navController.navigateToTopLevel(Routes.EXPENSES) },
                onSeeAllDues = { navController.navigateToTopLevel(Routes.DEBT_GOALS) },
                onAddEntry = { mainViewModel.openAddEntry(AddEntryMode.EXPENSE) }
            )
        }
        composable(Routes.EXPENSES) {
            ExpensesPlaceholderScreen()
        }
        composable(Routes.DEBT_GOALS) {
            DebtGoalsPlaceholderScreen()
        }
        composable(Routes.REMINDERS) {
            RemindersPlaceholderScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
