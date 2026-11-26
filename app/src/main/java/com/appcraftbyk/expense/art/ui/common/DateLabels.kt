package com.appcraftbyk.expense.art.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.appcraftbyk.expense.art.R
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Formats a month for chips and pickers, e.g. `Nov 2026`. */
@Composable
fun monthYearLabel(yearMonth: YearMonth): String {
    val locale = Locale.getDefault()
    return remember(yearMonth, locale) {
        "${yearMonth.month.getDisplayName(TextStyle.SHORT, locale)} ${yearMonth.year}"
    }
}

/** `Today`, `Yesterday` or a short date such as `24 Nov`. */
@Composable
fun relativeDayLabel(epochMillis: Long): String {
    val zone = remember { ZoneId.systemDefault() }
    val date = remember(epochMillis, zone) {
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    }
    val today = LocalDate.now(zone)
    return when (date) {
        today -> stringResource(R.string.home_date_today)
        today.minusDays(1) -> stringResource(R.string.home_date_yesterday)
        else -> shortDate(date)
    }
}

/** `Today, 2:30 PM`, `Yesterday, 2:30 PM` or `24 Nov`. */
@Composable
fun transactionWhenLabel(epochMillis: Long): String {
    val zone = remember { ZoneId.systemDefault() }
    val dateTime = remember(epochMillis, zone) {
        Instant.ofEpochMilli(epochMillis).atZone(zone)
    }
    val today = LocalDate.now(zone)
    val locale = Locale.getDefault()
    return when (dateTime.toLocalDate()) {
        today, today.minusDays(1) -> {
            val time = remember(dateTime, locale) {
                dateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            }
            "${relativeDayLabel(epochMillis)}, $time"
        }

        else -> shortDate(dateTime.toLocalDate())
    }
}

/** `Due today · 25 Nov`, `Due in 3 days · 28 Nov`, `Overdue by 2 days · 23 Nov`. */
@Composable
fun dueDateLabel(daysUntilDue: Long, dueEpochDay: Long): String {
    val relative = when {
        daysUntilDue < 0L -> {
            val days = (-daysUntilDue).toInt()
            pluralStringResource(R.plurals.home_due_overdue_days, days, days)
        }

        daysUntilDue == 0L -> stringResource(R.string.home_due_today)
        daysUntilDue == 1L -> stringResource(R.string.home_due_tomorrow)
        else -> {
            val days = daysUntilDue.toInt()
            pluralStringResource(R.plurals.home_due_in_days, days, days)
        }
    }
    return stringResource(R.string.home_due_with_date, relative, shortDate(LocalDate.ofEpochDay(dueEpochDay)))
}

@Composable
private fun shortDate(date: LocalDate): String {
    val locale = Locale.getDefault()
    return remember(date, locale) {
        date.format(DateTimeFormatter.ofPattern("d MMM", locale))
    }
}
