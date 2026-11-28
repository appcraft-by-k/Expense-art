package com.appcraftbyk.expense.art.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.navigation.AppTab
import com.appcraftbyk.expense.art.ui.theme.ExpenseArtTheme

private val LargeScreenMinWidth = 600.dp

@Composable
fun ExpenseArtApp(
    onQuickAddAction: (QuickAddAction) -> Unit = {},
    viewModel: ExpenseArtViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExpenseArtApp(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onAddClick = viewModel::onAddClick,
        onQuickAddDismiss = viewModel::onQuickAddDismissed,
        onQuickAddAction = onQuickAddAction
    )
}

@Composable
private fun ExpenseArtApp(
    uiState: ExpenseArtUiState,
    onTabSelected: (AppTab) -> Unit,
    onAddClick: () -> Unit,
    onQuickAddDismiss: () -> Unit,
    onQuickAddAction: (QuickAddAction) -> Unit
) {
    BackHandler(enabled = uiState.selectedTab != AppTab.Home) {
        onTabSelected(AppTab.Home)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth >= LargeScreenMinWidth

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_transaction)
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        val label = stringResource(tab.labelRes)
                        NavigationBarItem(
                            selected = tab == uiState.selectedTab,
                            onClick = { onTabSelected(tab) },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            EmptyTabContent(
                tab = uiState.selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        if (uiState.isQuickAddMenuVisible) {
            val onAction: (QuickAddAction) -> Unit = { action ->
                onQuickAddDismiss()
                onQuickAddAction(action)
            }

            if (isLargeScreen) {
                QuickAddDialog(
                    onDismiss = onQuickAddDismiss,
                    onAction = onAction
                )
            } else {
                QuickAddBottomSheet(
                    onDismiss = onQuickAddDismiss,
                    onAction = onAction
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddBottomSheet(
    onDismiss: () -> Unit,
    onAction: (QuickAddAction) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        QuickAddMenu(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            onAction = onAction
        )
    }
}

@Composable
private fun QuickAddDialog(
    onDismiss: () -> Unit,
    onAction: (QuickAddAction) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            QuickAddMenu(
                modifier = Modifier.padding(24.dp),
                onAction = onAction
            )
        }
    }
}

@Composable
private fun QuickAddMenu(
    onAction: (QuickAddAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.quick_add_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.quick_add_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAddAction.entries.forEach { action ->
                QuickAddMenuItem(
                    action = action,
                    onClick = { onAction(action) }
                )
            }
        }
    }
}

@Composable
private fun QuickAddMenuItem(
    action: QuickAddAction,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(action.iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = stringResource(action.labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun EmptyTabContent(tab: AppTab, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(tab.labelRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.empty_tab_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseArtAppPreview() {
    ExpenseArtTheme(dynamicColor = false) {
        ExpenseArtApp(
            uiState = ExpenseArtUiState(),
            onTabSelected = {},
            onAddClick = {},
            onQuickAddDismiss = {},
            onQuickAddAction = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun QuickAddMenuPreview() {
    ExpenseArtTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            QuickAddMenu(
                modifier = Modifier.padding(24.dp),
                onAction = {}
            )
        }
    }
}
