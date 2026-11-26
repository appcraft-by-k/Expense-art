package com.appcraftbyk.expense.art.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.ui.common.LoadStatus
import com.appcraftbyk.expense.art.ui.common.MonthPickerDialog
import com.appcraftbyk.expense.art.ui.common.monthYearLabel
import com.appcraftbyk.expense.art.ui.theme.expenseArtColors
import java.time.LocalTime
import java.time.YearMonth

/**
 * Home dashboard (Plan §13 P4). Layout follows the Stitch "Home Dashboard" reference, adapted to
 * the PRD tokens: 2×2 metrics, budget meter, top categories, upcoming dues and recent activity.
 */
@Composable
fun HomeScreen(
    selectedMonth: YearMonth,
    onSelectMonth: (YearMonth) -> Unit,
    onOpenSettings: () -> Unit,
    onViewLedger: () -> Unit,
    onSeeAllDues: () -> Unit,
    onAddEntry: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    LaunchedEffect(selectedMonth) { viewModel.setPeriod(selectedMonth) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val budgetEditor by viewModel.budgetEditor.collectAsStateWithLifecycle()
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeader(
            profileName = uiState.greetingName,
            onOpenSettings = onOpenSettings
        )

        when (uiState.status) {
            LoadStatus.Loading -> HomeLoadingState()
            LoadStatus.Error -> HomeErrorState(onRetry = viewModel::retry)
            LoadStatus.Content, LoadStatus.Empty -> HomeContent(
                uiState = uiState,
                selectedMonth = selectedMonth,
                onOpenMonthPicker = { showMonthPicker = true },
                onOpenBudgetEditor = viewModel::openBudgetEditor,
                onSeeAllDues = onSeeAllDues,
                onViewLedger = onViewLedger,
                onAddEntry = onAddEntry
            )
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            initialMonth = selectedMonth,
            onMonthSelected = {
                onSelectMonth(it)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (budgetEditor.isOpen) {
        BudgetEditorSheet(
            state = budgetEditor,
            onInputChange = viewModel::onBudgetInputChange,
            onSave = viewModel::saveBudget,
            onRemove = viewModel::removeBudget,
            onDismiss = viewModel::dismissBudgetEditor
        )
    }
}

@Composable
private fun HomeHeader(
    profileName: String?,
    onOpenSettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CurrencyRupee,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_app_name),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.nav_home),
                style = MaterialTheme.typography.titleMedium
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.action_settings),
                tint = colors.onSurfaceVariant
            )
        }
        Surface(
            onClick = onOpenSettings,
            shape = CircleShape,
            color = colors.primary,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val initial = profileName?.firstOrNull()?.uppercaseChar()?.toString()
                if (initial != null) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = stringResource(R.string.home_avatar),
                        tint = colors.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    selectedMonth: YearMonth,
    onOpenMonthPicker: () -> Unit,
    onOpenBudgetEditor: () -> Unit,
    onSeeAllDues: () -> Unit,
    onViewLedger: () -> Unit,
    onAddEntry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            // Extra bottom inset keeps the last card clear of the center FAB.
            .padding(top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeGreeting(
            uiState = uiState,
            selectedMonth = selectedMonth,
            onOpenMonthPicker = onOpenMonthPicker
        )

        HomeMetricGrid(uiState)

        if (uiState.isMonthEmpty) {
            HomeEmptyMonthCard(onAddEntry = onAddEntry)
        } else {
            HomeBudgetCard(uiState = uiState, onEdit = onOpenBudgetEditor)
            if (uiState.topCategories.isNotEmpty() || uiState.otherSpendMinor > 0L) {
                HomeCategoryCard(uiState)
            }
        }

        if (uiState.upcomingDues.isNotEmpty()) {
            HomeDuesCard(dues = uiState.upcomingDues, onSeeAll = onSeeAllDues)
        }

        if (uiState.recentTransactions.isNotEmpty()) {
            HomeActivityCard(
                transactions = uiState.recentTransactions,
                onViewLedger = onViewLedger
            )
        }
    }
}

@Composable
private fun HomeGreeting(
    uiState: HomeUiState,
    selectedMonth: YearMonth,
    onOpenMonthPicker: () -> Unit
) {
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> stringResource(R.string.home_greeting_morning)
        in 12..16 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_evening)
    }
    val name = uiState.greetingName
    val greetingText = if (name != null) {
        stringResource(R.string.home_greeting_with_name, greeting, name)
    } else {
        stringResource(R.string.home_greeting_without_name, greeting)
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = greetingText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            uiState.budgetHealth?.let { health ->
                BudgetStatusPill(health)
            }
        }
        Text(
            text = stringResource(R.string.home_overview),
            modifier = Modifier.padding(top = 2.dp),
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(Modifier.height(10.dp))
        PeriodChip(
            selectedMonth = selectedMonth,
            onClick = onOpenMonthPicker
        )
    }
}

@Composable
private fun BudgetStatusPill(health: BudgetHealth) {
    val colors = MaterialTheme.colorScheme
    val semantic = MaterialTheme.expenseArtColors
    val (text, container, content) = when (health) {
        BudgetHealth.ON_TRACK -> Triple(
            stringResource(R.string.home_status_on_track),
            semantic.positiveContainer,
            semantic.onPositiveContainer
        )

        BudgetHealth.NEAR_LIMIT -> Triple(
            stringResource(R.string.home_status_near_limit),
            semantic.warningContainer,
            semantic.onWarningContainer
        )

        BudgetHealth.OVER_BUDGET -> Triple(
            stringResource(R.string.home_status_over_budget),
            semantic.negativeContainer,
            semantic.onNegativeContainer
        )
    }

    Surface(
        shape = CircleShape,
        color = container
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = content,
            maxLines = 1
        )
    }
}

@Composable
private fun PeriodChip(
    selectedMonth: YearMonth,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val label = if (selectedMonth == YearMonth.now()) {
        stringResource(
            R.string.home_period_this_month,
            monthYearLabel(selectedMonth)
        )
    } else {
        stringResource(R.string.home_period_month, monthYearLabel(selectedMonth))
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeErrorState(onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.expenseArtColors.negative,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.home_error_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_error_message),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.home_retry))
        }
    }
}
