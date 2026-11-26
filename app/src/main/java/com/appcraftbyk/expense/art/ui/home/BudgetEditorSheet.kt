package com.appcraftbyk.expense.art.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.core.money.AmountParser
import com.appcraftbyk.expense.art.core.money.IndianCurrencyFormatter

/** Edits (or clears) the single monthly budget; Plan §13 P4. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(
    state: BudgetEditorState,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val money = remember { IndianCurrencyFormatter() }
    val preview = AmountParser.parseRupeesToMinor(state.input)
        ?.takeIf { it > 0L }
        ?.let(money::format)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_budget_editor_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.home_budget_editor_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.home_budget_editor_label)) },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = state.error != null,
                supportingText = {
                    when (state.error) {
                        BudgetEditorError.INVALID_AMOUNT -> Text(
                            stringResource(R.string.home_budget_editor_invalid)
                        )

                        BudgetEditorError.SAVE_FAILED -> Text(
                            stringResource(R.string.home_budget_editor_save_failed)
                        )

                        null -> Text(preview.orEmpty())
                    }
                }
            )

            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.action_save))
                }
            }

            if (state.hasExistingBudget) {
                TextButton(
                    onClick = onRemove,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.home_budget_remove))
                }
            }
        }
    }
}
