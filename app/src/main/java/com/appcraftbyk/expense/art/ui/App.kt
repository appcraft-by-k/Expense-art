package com.appcraftbyk.expense.art.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.addentry.AddEntryPlaceholderSheet
import com.appcraftbyk.expense.art.ui.main.MainViewModel
import com.appcraftbyk.expense.art.ui.navigation.ExpenseArtNavHost
import com.appcraftbyk.expense.art.ui.navigation.TopLevelDestination
import com.appcraftbyk.expense.art.ui.navigation.navigateToTopLevel

/**
 * App shell: four bottom-bar destinations, a center FAB whose preset follows the tab, and the
 * Settings detail route. Top-level navigation keeps a saved back stack per tab.
 */
@Composable
fun ExpenseArtApp(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val addEntrySheet by mainViewModel.addEntrySheet.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = TopLevelDestination.entries
        .firstOrNull { it.route == backStackEntry?.destination?.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Screens handle their own top/side insets; the shell only reserves the bottom bar height.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentDestination != null) {
                ExpenseArtBottomBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        },
        floatingActionButton = {
            val addEntryMode = currentDestination?.addEntryMode
            if (addEntryMode != null) {
                FloatingActionButton(
                    onClick = { mainViewModel.openAddEntry(addEntryMode) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.action_add_entry)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        ExpenseArtNavHost(
            navController = navController,
            mainViewModel = mainViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (addEntrySheet.isOpen) {
        AddEntryPlaceholderSheet(
            state = addEntrySheet,
            onDismiss = mainViewModel::dismissAddEntry
        )
    }
}

@Composable
private fun ExpenseArtBottomBar(
    navController: NavHostController,
    currentDestination: TopLevelDestination
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == currentDestination,
                onClick = {
                    if (destination != currentDestination) {
                        navController.navigateToTopLevel(destination.route)
                    }
                },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
