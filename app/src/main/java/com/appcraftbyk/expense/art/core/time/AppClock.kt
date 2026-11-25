package com.appcraftbyk.expense.art.core.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Single source of "now" for the app. Tests inject a fixed [Clock] so month boundaries and
 * overdue calculations stay deterministic.
 */
class AppClock(private val clock: Clock = Clock.systemDefaultZone()) {

    val zone: ZoneId get() = clock.zone

    fun nowMillis(): Long = clock.millis()

    fun todayEpochDay(): Long = LocalDate.now(clock).toEpochDay()

    fun currentYearMonth(): YearMonth = YearMonth.now(clock)

    fun yearMonthOf(epochMillis: Long): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(clock.zone).toLocalDate())

    fun epochDayOf(epochMillis: Long): Long =
        Instant.ofEpochMilli(epochMillis).atZone(clock.zone).toLocalDate().toEpochDay()
}
