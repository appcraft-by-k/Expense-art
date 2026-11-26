package com.appcraftbyk.expense.art.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appcraftbyk.expense.art.R
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Month + year picker shared by Home (P4) and the ledger (P5). */
@Composable
fun MonthPickerDialog(
    initialMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember(initialMonth) { mutableIntStateOf(initialMonth.year) }
    val locale = Locale.getDefault()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.month_picker_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { year -= 1 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.month_picker_previous_year)
                        )
                    }
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { year += 1 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.month_picker_next_year)
                        )
                    }
                }

                Month.values().toList().chunked(3).forEach { months ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        months.forEach { month ->
                            val selected = year == initialMonth.year &&
                                month == initialMonth.month
                            FilterChip(
                                selected = selected,
                                onClick = { onMonthSelected(YearMonth.of(year, month)) },
                                label = {
                                    Text(
                                        text = month.getDisplayName(TextStyle.SHORT, locale),
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - months.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
