package com.appcraftbyk.expense.art.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.core.money.IndianCurrencyFormatter
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.ui.common.dueDateLabel
import com.appcraftbyk.expense.art.ui.common.transactionWhenLabel
import com.appcraftbyk.expense.art.ui.theme.expenseArtColors
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private data class MetricChip(
    val text: String,
    val container: Color,
    val content: Color
)

@Composable
fun HomeMetricGrid(uiState: HomeUiState) {
    val money = remember { IndianCurrencyFormatter() }
    val colors = MaterialTheme.colorScheme
    val semantic = MaterialTheme.expenseArtColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Filled.SouthWest,
                iconTint = semantic.positive,
                iconContainer = semantic.positiveContainer,
                label = stringResource(R.string.home_metric_income),
                amount = money.format(uiState.incomeMinor),
                chip = incomeChip(uiState)
            )
            MetricCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Filled.NorthEast,
                iconTint = semantic.negative,
                iconContainer = semantic.negativeContainer,
                label = stringResource(R.string.home_metric_spent),
                amount = money.format(uiState.expenseMinor),
                chip = expenseChip(uiState)
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Filled.Handshake,
                iconTint = colors.onSurfaceVariant,
                iconContainer = colors.surfaceContainerHigh,
                label = stringResource(R.string.home_metric_debt),
                // Debt directions are never netted (Plan §7); the card shows what the user owes.
                amount = money.format(uiState.oweMinor),
                chip = debtChip(uiState, money)
            )
            MetricCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Filled.Savings,
                iconTint = colors.onPrimaryContainer,
                iconContainer = colors.primaryContainer,
                label = stringResource(R.string.home_metric_savings),
                amount = money.format(uiState.totalSavingsMinor),
                chip = savingsChip(uiState)
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconTint: Color,
    iconContainer: Color,
    label: String,
    amount: String,
    chip: MetricChip,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = chip.container
            ) {
                Text(
                    text = chip.text,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = chip.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun incomeChip(uiState: HomeUiState): MetricChip {
    val semantic = MaterialTheme.expenseArtColors
    val neutral = MaterialTheme.colorScheme.surfaceContainerHigh
    val onNeutral = MaterialTheme.colorScheme.onSurfaceVariant
    val delta = uiState.incomeDeltaPercent
    return when {
        delta != null && delta > 0 -> MetricChip(
            text = stringResource(R.string.home_income_delta_up, delta),
            container = semantic.positiveContainer,
            content = semantic.onPositiveContainer
        )

        delta != null && delta < 0 -> MetricChip(
            text = stringResource(R.string.home_income_delta_down, delta),
            container = semantic.negativeContainer,
            content = semantic.onNegativeContainer
        )

        delta != null -> MetricChip(
            text = stringResource(R.string.home_income_delta_flat),
            container = neutral,
            content = onNeutral
        )

        uiState.incomeMinor > 0L -> MetricChip(
            text = stringResource(R.string.home_income_new),
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer
        )

        else -> MetricChip(
            text = stringResource(R.string.home_income_none),
            container = neutral,
            content = onNeutral
        )
    }
}

@Composable
private fun expenseChip(uiState: HomeUiState): MetricChip {
    val semantic = MaterialTheme.expenseArtColors
    val neutral = MaterialTheme.colorScheme.surfaceContainerHigh
    val onNeutral = MaterialTheme.colorScheme.onSurfaceVariant
    val share = uiState.expenseShareOfIncomePercent
    return when {
        share != null -> MetricChip(
            text = stringResource(R.string.home_expense_share_of_income, share),
            container = semantic.negativeContainer,
            content = semantic.onNegativeContainer
        )

        uiState.expenseMinor > 0L -> MetricChip(
            text = stringResource(R.string.home_expense_without_income),
            container = semantic.warningContainer,
            content = semantic.onWarningContainer
        )

        else -> MetricChip(
            text = stringResource(R.string.home_expense_none),
            container = neutral,
            content = onNeutral
        )
    }
}

@Composable
private fun debtChip(uiState: HomeUiState, money: IndianCurrencyFormatter): MetricChip {
    val neutral = MaterialTheme.colorScheme.surfaceContainerHigh
    val onNeutral = MaterialTheme.colorScheme.onSurfaceVariant
    val owes = uiState.oweMinor
    val owedToMe = uiState.owedToMeMinor
    val text = when {
        owes == 0L && owedToMe == 0L -> stringResource(R.string.home_debt_none)
        // The card amount already shows what the user owes.
        owedToMe == 0L -> stringResource(R.string.home_debt_all_owed)
        else -> stringResource(R.string.home_debt_owed_to_you, money.format(owedToMe))
    }
    return MetricChip(text = text, container = neutral, content = onNeutral)
}

@Composable
private fun savingsChip(uiState: HomeUiState): MetricChip {
    val colors = MaterialTheme.colorScheme
    return if (uiState.activeGoalCount > 0) {
        MetricChip(
            text = pluralStringResource(
                R.plurals.home_active_goals,
                uiState.activeGoalCount,
                uiState.activeGoalCount
            ),
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer
        )
    } else {
        MetricChip(
            text = stringResource(R.string.home_savings_none),
            container = colors.surfaceContainerHigh,
            content = colors.onSurfaceVariant
        )
    }
}

@Composable
fun HomeBudgetCard(
    uiState: HomeUiState,
    onEdit: () -> Unit
) {
    val money = remember { IndianCurrencyFormatter() }
    val colors = MaterialTheme.colorScheme
    val semantic = MaterialTheme.expenseArtColors
    val limit = uiState.budgetLimitMinor

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (limit == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_budget_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.home_budget_empty_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Button(onClick = onEdit) {
                    Text(stringResource(R.string.home_budget_set))
                }
            }
            return@Card
        }

        val remaining = uiState.budgetRemainingMinor ?: 0L
        val overBudget = uiState.budgetHealth == BudgetHealth.OVER_BUDGET
        val remainingPercent = (100 - (uiState.budgetUsedFraction * 100).roundToInt())
            .coerceIn(0, 100)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = stringResource(R.string.home_budget_edit),
                    onClick = onEdit
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.home_budget_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.home_budget_edit_short),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.home_budget_spent_of,
                            money.format(uiState.expenseMinor),
                            money.format(limit)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (overBudget) {
                            stringResource(
                                R.string.home_budget_over,
                                money.format(remaining.absoluteValue)
                            )
                        } else {
                            stringResource(R.string.home_budget_left, money.format(remaining))
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (overBudget) semantic.negative else colors.primary
                    )
                    Text(
                        text = if (overBudget) {
                            stringResource(R.string.home_budget_over_label)
                        } else {
                            stringResource(R.string.home_budget_remaining, remainingPercent)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            BudgetProgressBar(
                fraction = uiState.budgetUsedFraction,
                isOverBudget = overBudget
            )
        }
    }
}

@Composable
private fun BudgetProgressBar(fraction: Float, isOverBudget: Boolean) {
    val colors = MaterialTheme.colorScheme
    val fill = if (isOverBudget) {
        MaterialTheme.expenseArtColors.negative
    } else {
        colors.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .progressSemantics(fraction)
            .clip(CircleShape)
            .background(colors.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(fill)
        )
        // 80% caution marker from the Stitch reference.
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(colors.onSurfaceVariant.copy(alpha = 0.35f))
            )
        }
    }
}

@Composable
fun HomeCategoryCard(uiState: HomeUiState) {
    val money = remember { IndianCurrencyFormatter() }
    val colors = MaterialTheme.colorScheme
    val semantic = MaterialTheme.expenseArtColors
    val dotColors = listOf(
        colors.primary,
        semantic.positive,
        semantic.negative,
        colors.outline
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.home_categories_title),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
                ),
                color = colors.onSurfaceVariant
            )

            uiState.topCategories.chunked(2).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEachIndexed { columnIndex, item ->
                        CategoryTag(
                            modifier = Modifier.weight(1f),
                            name = item.name
                                ?: stringResource(R.string.home_categories_uncategorized),
                            amount = money.format(item.amountMinor),
                            dotColor = dotColors[(rowIndex * 2 + columnIndex) % dotColors.size]
                        )
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (uiState.otherSpendMinor > 0L) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_categories_others),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = money.format(uiState.otherSpendMinor),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTag(
    name: String,
    amount: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
            ),
            maxLines = 1
        )
    }
}

@Composable
fun HomeDuesCard(
    dues: List<HomeDueRow>,
    onSeeAll: () -> Unit
) {
    val money = remember { IndianCurrencyFormatter() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_dues_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onSeeAll) {
                    Text(stringResource(R.string.home_dues_see_all, dues.size))
                }
            }

            dues.forEach { due ->
                DueRow(due = due, money = money)
            }
        }
    }
}

@Composable
private fun DueRow(due: HomeDueRow, money: IndianCurrencyFormatter) {
    val colors = MaterialTheme.colorScheme
    val semantic = MaterialTheme.expenseArtColors
    val urgency = when {
        due.daysUntilDue < 0L -> DueUrgency.OVERDUE
        due.daysUntilDue <= 3L -> DueUrgency.URGENT
        due.daysUntilDue <= 7L -> DueUrgency.PENDING
        else -> DueUrgency.SCHEDULED
    }
    val (statusText, statusContainer, statusContent) = when (urgency) {
        DueUrgency.OVERDUE -> Triple(
            stringResource(R.string.home_due_status_overdue),
            semantic.negativeContainer,
            semantic.onNegativeContainer
        )

        DueUrgency.URGENT -> Triple(
            stringResource(R.string.home_due_status_urgent),
            semantic.negativeContainer,
            semantic.onNegativeContainer
        )

        DueUrgency.PENDING -> Triple(
            stringResource(R.string.home_due_status_pending),
            colors.surfaceContainerHighest,
            colors.onSurfaceVariant
        )

        DueUrgency.SCHEDULED -> Triple(
            stringResource(R.string.home_due_status_scheduled),
            semantic.positiveContainer,
            semantic.onPositiveContainer
        )
    }
    val directionLabel = when (due.direction) {
        DebtDirection.BORROWED -> stringResource(R.string.home_due_borrowed)
        DebtDirection.LENT -> stringResource(R.string.home_due_lent)
    }
    val icon = when (due.direction) {
        DebtDirection.BORROWED -> Icons.AutoMirrored.Filled.CallMade
        DebtDirection.LENT -> Icons.AutoMirrored.Filled.CallReceived
    }
    val iconTint = when (due.direction) {
        DebtDirection.BORROWED -> semantic.negative
        DebtDirection.LENT -> semantic.positive
    }
    val iconContainer = when (due.direction) {
        DebtDirection.BORROWED -> semantic.negativeContainer
        DebtDirection.LENT -> semantic.positiveContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerLow)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = due.contactName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.home_due_direction_and_date,
                    directionLabel,
                    dueDateLabel(due.daysUntilDue, due.dueEpochDay)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (urgency == DueUrgency.OVERDUE) semantic.negative else colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = money.format(due.amountMinor),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                ),
                maxLines = 1
            )
            Surface(
                shape = CircleShape,
                color = statusContainer
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusContent,
                    maxLines = 1
                )
            }
        }
    }
}

private enum class DueUrgency { OVERDUE, URGENT, PENDING, SCHEDULED }

@Composable
fun HomeActivityCard(
    transactions: List<HomeTransactionRow>,
    onViewLedger: () -> Unit
) {
    val money = remember { IndianCurrencyFormatter() }
    val colors = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_activity_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onViewLedger) {
                    Text(stringResource(R.string.home_activity_view_ledger))
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_activity_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            } else {
                transactions.forEach { transaction ->
                    ActivityRow(transaction = transaction, money = money)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(transaction: HomeTransactionRow, money: IndianCurrencyFormatter) {
    val colors = MaterialTheme.colorScheme
    val semantic = MaterialTheme.expenseArtColors
    val isIncome = transaction.kind == TransactionKind.INCOME
    val whenLabel = transactionWhenLabel(transaction.occurredAt)
    val subtitle = listOfNotNull(
        transaction.categoryName?.takeIf { it != transaction.title },
        whenLabel
    ).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isIncome) semantic.positiveContainer else colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon(transaction.categoryIconKey, transaction.kind),
                contentDescription = null,
                tint = if (isIncome) semantic.positive else colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = transaction.title
                    ?: stringResource(R.string.home_activity_unknown),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = (if (isIncome) "+" else "-") + money.format(transaction.amountMinor),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
            ),
            color = if (isIncome) semantic.positive else colors.onSurface,
            maxLines = 1
        )
    }
}

private fun categoryIcon(iconKey: String?, kind: TransactionKind): ImageVector = when (iconKey) {
    "food" -> Icons.Filled.Restaurant
    "groceries" -> Icons.Filled.ShoppingBasket
    "transport" -> Icons.Filled.DirectionsCar
    "utilities" -> Icons.Filled.Bolt
    "shopping" -> Icons.Filled.ShoppingBag
    "health" -> Icons.Filled.LocalHospital
    "entertainment" -> Icons.Filled.Movie
    "rent" -> Icons.Filled.Home
    "salary" -> Icons.Filled.Payments
    "freelance" -> Icons.Filled.Work
    "business" -> Icons.Filled.BusinessCenter
    "investments" -> Icons.AutoMirrored.Filled.TrendingUp
    else -> if (kind == TransactionKind.INCOME) {
        Icons.Filled.Payments
    } else {
        Icons.AutoMirrored.Filled.ReceiptLong
    }
}

@Composable
fun HomeEmptyMonthCard(onAddEntry: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_empty_message),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAddEntry) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_empty_action))
            }
        }
    }
}
