package com.appcraftbyk.expense.art.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcraftbyk.expense.art.ExpenseArtApplication
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.data.local.CategoryEntity
import com.appcraftbyk.expense.art.data.local.PaymentMethodEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import com.appcraftbyk.expense.art.ui.navigation.AppTab
import com.appcraftbyk.expense.art.ui.theme.ExpenseArtTheme
import kotlinx.coroutines.launch

private val LargeScreenMinWidth = 600.dp

@Composable
fun ExpenseArtApp(viewModel: ExpenseArtViewModel = viewModel()) {
    val context = LocalContext.current
    val application = context.applicationContext as ExpenseArtApplication
    val addEntryViewModel: AddEntryViewModel = viewModel(
        factory = remember(application) { AddEntryViewModel.factory(application) }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entryState by addEntryViewModel.uiState.collectAsStateWithLifecycle()
    val savedEntryId by addEntryViewModel.savedEntryId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.entry_saved)

    LaunchedEffect(savedEntryId) {
        if (savedEntryId == null) return@LaunchedEffect

        viewModel.onSheetDismissed()
        addEntryViewModel.consumeSaved()
        scope.launch {
            snackbarHostState.showSnackbar(savedMessage)
        }
    }

    val entryActions = remember(addEntryViewModel) {
        AddEntryActions(
            onTypeSelected = addEntryViewModel::onTypeSelected,
            onAmountChanged = addEntryViewModel::onAmountChanged,
            onQuickAmountAdded = addEntryViewModel::onQuickAmountAdded,
            onClearAmount = addEntryViewModel::onClearAmount,
            onCategorySelected = addEntryViewModel::onCategorySelected,
            onPaymentMethodSelected = addEntryViewModel::onPaymentMethodSelected,
            onDateTimeSelected = addEntryViewModel::onDateTimeSelected,
            onNoteChanged = addEntryViewModel::onNoteChanged,
            onSave = addEntryViewModel::save
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth >= LargeScreenMinWidth

        ExpenseArtAppContent(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onTabSelected = viewModel::onTabSelected,
            onAddClick = {
                addEntryViewModel.start(TransactionType.EXPENSE)
                viewModel.onAddClick()
            }
        )

        when (uiState.sheet) {
            AppSheet.None -> Unit

            AppSheet.AddEntry -> {
                if (isLargeScreen) {
                    AddEntryDialog(
                        state = entryState,
                        actions = entryActions,
                        onDismiss = viewModel::onSheetDismissed
                    )
                } else {
                    AddEntryBottomSheet(
                        state = entryState,
                        actions = entryActions,
                        onDismiss = viewModel::onSheetDismissed
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseArtAppContent(
    uiState: ExpenseArtUiState,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (AppTab) -> Unit,
    onAddClick: () -> Unit
) {
    BackHandler(enabled = uiState.selectedTab != AppTab.Home) {
        onTabSelected(AppTab.Home)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        ExpenseArtAppContent(
            uiState = ExpenseArtUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onTabSelected = {},
            onAddClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun AddEntryFormPreview() {
    ExpenseArtTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            AddEntryForm(
                state = AddEntryUiState(
                    type = TransactionType.EXPENSE,
                    amountInput = "1450",
                    amountMinor = 145_000,
                    categories = listOf(
                        CategoryEntity(
                            id = 1,
                            name = "Food & Dining",
                            type = TransactionType.EXPENSE,
                            isDefault = true
                        )
                    ),
                    selectedCategoryId = 1,
                    isCategoriesLoaded = true,
                    paymentMethods = listOf(
                        PaymentMethodEntity(
                            id = 1,
                            name = "UPI",
                            isDefault = true
                        )
                    ),
                    selectedPaymentMethodId = 1,
                    isPaymentMethodsLoaded = true,
                    occurredAt = System.currentTimeMillis(),
                    note = "Dinner with team"
                ),
                actions = AddEntryActions(
                    onTypeSelected = {},
                    onAmountChanged = {},
                    onQuickAmountAdded = {},
                    onClearAmount = {},
                    onCategorySelected = {},
                    onPaymentMethodSelected = {},
                    onDateTimeSelected = {},
                    onNoteChanged = {},
                    onSave = {}
                ),
                onDismiss = {},
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
