package com.appcraftbyk.expense.art.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.data.local.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Form events owned by [AddEntryViewModel]. */
internal data class AddEntryActions(
    val onTypeSelected: (TransactionType) -> Unit,
    val onAmountChanged: (String) -> Unit,
    val onQuickAmountAdded: (Long) -> Unit,
    val onClearAmount: () -> Unit,
    val onCategorySelected: (Long) -> Unit,
    val onPaymentMethodSelected: (Long?) -> Unit,
    val onDateTimeSelected: (Long) -> Unit,
    val onNoteChanged: (String) -> Unit,
    val onSave: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEntryBottomSheet(
    state: AddEntryUiState,
    actions: AddEntryActions,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        AddEntryForm(
            state = state,
            actions = actions,
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
internal fun AddEntryDialog(
    state: AddEntryUiState,
    actions: AddEntryActions,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .heightIn(max = 840.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            AddEntryForm(
                state = state,
                actions = actions,
                onDismiss = onDismiss,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Composable
internal fun AddEntryForm(
    state: AddEntryUiState,
    actions: AddEntryActions,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.entry_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                enabled = !state.isSaving
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.entry_close)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            EntryTypeSelector(
                selectedType = state.type,
                enabled = !state.isSaving,
                onTypeSelected = actions.onTypeSelected
            )

            Spacer(Modifier.height(12.dp))

            AmountInput(
                state = state,
                enabled = !state.isSaving,
                onAmountChanged = actions.onAmountChanged,
                onQuickAmountAdded = actions.onQuickAmountAdded,
                onClearAmount = actions.onClearAmount
            )

            Spacer(Modifier.height(12.dp))

            CategorySelector(
                state = state,
                enabled = !state.isSaving,
                onCategorySelected = actions.onCategorySelected
            )

            Spacer(Modifier.height(8.dp))

            DateTimeField(
                occurredAt = state.occurredAt,
                enabled = !state.isSaving,
                onDateTimeSelected = actions.onDateTimeSelected
            )

            Spacer(Modifier.height(8.dp))

            PaymentMethodSelector(
                state = state,
                enabled = !state.isSaving,
                onPaymentMethodSelected = actions.onPaymentMethodSelected
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.note,
                onValueChange = actions.onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                label = { Text(stringResource(R.string.entry_note_label)) },
                placeholder = { Text(stringResource(R.string.entry_note_placeholder)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit_note),
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (state.amountError) {
                Text(
                    text = stringResource(R.string.entry_amount_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.saveError) {
                Text(
                    text = stringResource(R.string.entry_save_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.entry_cancel))
            }
            Button(
                onClick = actions.onSave,
                enabled = state.canSave,
                modifier = Modifier.weight(2f)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (state.type == TransactionType.EXPENSE) {
                            R.string.entry_save_expense
                        } else {
                            R.string.entry_save_income
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun EntryTypeSelector(
    selectedType: TransactionType,
    enabled: Boolean,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TransactionType.entries.forEach { type ->
            val selected = type == selectedType
            Surface(
                onClick = { onTypeSelected(type) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (type == TransactionType.EXPENSE) {
                                R.drawable.ic_arrow_downward
                            } else {
                                R.drawable.ic_arrow_upward
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(
                            if (type == TransactionType.EXPENSE) {
                                R.string.entry_type_expense
                            } else {
                                R.string.entry_type_income
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountInput(
    state: AddEntryUiState,
    enabled: Boolean,
    onAmountChanged: (String) -> Unit,
    onQuickAmountAdded: (Long) -> Unit,
    onClearAmount: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.entry_amount_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.currency_symbol),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                BasicTextField(
                    value = state.amountInput,
                    onValueChange = onAmountChanged,
                    enabled = enabled,
                    modifier = Modifier.widthIn(min = 48.dp),
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (state.amountInput.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.entry_amount_zero),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AmountChip(
                    text = stringResource(R.string.entry_quick_add_100),
                    enabled = enabled,
                    onClick = { onQuickAmountAdded(10_000) }
                )
                AmountChip(
                    text = stringResource(R.string.entry_quick_add_500),
                    enabled = enabled,
                    onClick = { onQuickAmountAdded(50_000) }
                )
                AmountChip(
                    text = stringResource(R.string.entry_quick_add_1000),
                    enabled = enabled,
                    onClick = { onQuickAmountAdded(100_000) }
                )
                AmountChip(
                    text = stringResource(R.string.entry_clear),
                    enabled = enabled,
                    onClick = onClearAmount
                )
            }
        }
    }
}

@Composable
private fun AmountChip(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CategorySelector(
    state: AddEntryUiState,
    enabled: Boolean,
    onCategorySelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.categories.firstOrNull { it.id == state.selectedCategoryId }
    val value = when {
        selected != null -> selected.name
        !state.isCategoriesLoaded -> stringResource(R.string.entry_category_loading)
        else -> stringResource(R.string.entry_category_empty)
    }

    Box {
        SelectorField(
            label = stringResource(R.string.entry_category_label),
            value = value,
            iconRes = if (state.type == TransactionType.EXPENSE) {
                R.drawable.ic_tab_expenses
            } else {
                R.drawable.ic_income
            },
            enabled = enabled && state.categories.isNotEmpty(),
            onClick = { expanded = true },
            trailing = {
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            state.categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
    if (state.categories.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.categories.forEach { category ->
                FilterChip(
                    selected = category.id == state.selectedCategoryId,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodSelector(
    state: AddEntryUiState,
    enabled: Boolean,
    onPaymentMethodSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.paymentMethods.firstOrNull { it.id == state.selectedPaymentMethodId }
    val value = when {
        selected != null -> selected.name
        !state.isPaymentMethodsLoaded -> stringResource(R.string.entry_payment_loading)
        else -> stringResource(R.string.entry_payment_empty)
    }

    Box {
        SelectorField(
            label = stringResource(R.string.entry_payment_label),
            value = value,
            iconRes = R.drawable.ic_wallet,
            enabled = enabled && state.paymentMethods.isNotEmpty(),
            onClick = { expanded = true },
            trailing = {
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            state.paymentMethods.forEach { paymentMethod ->
                DropdownMenuItem(
                    text = { Text(paymentMethod.name) },
                    onClick = {
                        onPaymentMethodSelected(paymentMethod.id)
                        expanded = false
                    }
                )
            }
        }
    }
    if (state.paymentMethods.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.paymentMethods.forEach { paymentMethod ->
                FilterChip(
                    selected = paymentMethod.id == state.selectedPaymentMethodId,
                    onClick = { onPaymentMethodSelected(paymentMethod.id) },
                    label = { Text(paymentMethod.name) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun DateTimeField(
    occurredAt: Long,
    enabled: Boolean,
    onDateTimeSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val showPicker = {
        val calendar = Calendar.getInstance().apply { timeInMillis = occurredAt }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    timeInMillis = occurredAt
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        selected.set(Calendar.HOUR_OF_DAY, hour)
                        selected.set(Calendar.MINUTE, minute)
                        selected.set(Calendar.SECOND, 0)
                        selected.set(Calendar.MILLISECOND, 0)
                        onDateTimeSelected(selected.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    SelectorField(
        label = stringResource(R.string.entry_date_label),
        value = formatEntryDateTime(occurredAt),
        iconRes = R.drawable.ic_calendar_today,
        enabled = enabled,
        onClick = showPicker,
        trailing = {
            Text(
                text = stringResource(R.string.entry_date_change),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
private fun SelectorField(
    label: String,
    value: String,
    iconRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                trailing()
            }
        }
    }
}

@Composable
private fun formatEntryDateTime(occurredAt: Long): String {
    val date = Date(occurredAt)
    val dateText = if (DateUtils.isToday(occurredAt)) {
        stringResource(R.string.entry_today)
    } else {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }
    val timeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    return "$dateText • $timeText"
}
