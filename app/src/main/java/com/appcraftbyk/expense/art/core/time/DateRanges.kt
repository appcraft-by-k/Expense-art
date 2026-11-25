package com.appcraftbyk.expense.art.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/** Half-open epoch-millis range: [fromMillis, toMillisExclusive). */
data class EpochRange(val fromMillis: Long, val toMillisExclusive: Long) {
    operator fun contains(epochMillis: Long): Boolean =
        epochMillis >= fromMillis && epochMillis < toMillisExclusive
}

/** Calendar ranges derived from [AppClock] so a timezone change cannot silently shift a month. */
class DateRanges(private val appClock: AppClock) {

    fun month(yearMonth: YearMonth): EpochRange {
        val zone = appClock.zone
        val start = yearMonth.atDay(1).atStartOfDay(zone)
        val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone)
        return EpochRange(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
    }

    fun day(epochDay: Long): EpochRange {
        val zone = appClock.zone
        val date = LocalDate.ofEpochDay(epochDay)
        return EpochRange(
            fromMillis = date.atStartOfDay(zone).toInstant().toEpochMilli(),
            toMillisExclusive = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        )
    }

    fun yearMonthOf(epochMillis: Long): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(appClock.zone).toLocalDate())

    fun epochDayOf(epochMillis: Long): Long =
        Instant.ofEpochMilli(epochMillis).atZone(appClock.zone).toLocalDate().toEpochDay()
}

/** `202611` key used by `monthly_budgets`; keeps the DB column independent of java.time. */
fun YearMonth.toBudgetKey(): Int = year * 100 + monthValue

fun budgetKeyToYearMonth(key: Int): YearMonth = YearMonth.of(key / 100, key % 100)
