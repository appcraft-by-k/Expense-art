package com.appcraftbyk.expense.art.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class DateRangesTest {

    private val zone = ZoneId.of("Asia/Kolkata")
    private val clock = AppClock(
        Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), zone)
    )
    private val dateRanges = DateRanges(clock)

    @Test
    fun monthRange_isHalfOpenInAppZone() {
        val range = dateRanges.month(YearMonth.of(2026, 11))

        assertEquals(1_793_471_400_000L, range.fromMillis) // 2026-11-01T00:00+05:30
        assertEquals(1_796_063_400_000L, range.toMillisExclusive) // 2026-12-01T00:00+05:30
        assertFalse(1_793_471_400_000L - 1 in range)
        assertTrue(1_793_471_400_000L in range)
        assertFalse(range.toMillisExclusive in range)
    }

    @Test
    fun dayRange_coversLocalCalendarDay() {
        val epochDay = LocalDate.of(2026, 11, 15).toEpochDay()

        val range = dateRanges.day(epochDay)

        assertEquals(1_794_681_000_000L, range.fromMillis)
        assertEquals(range.fromMillis + 86_400_000L, range.toMillisExclusive)
    }

    @Test
    fun convertsBetweenMillisAndCalendarValues() {
        val noonIst = Instant.parse("2026-11-15T06:30:00Z").toEpochMilli()

        assertEquals(YearMonth.of(2026, 11), dateRanges.yearMonthOf(noonIst))
        assertEquals(LocalDate.of(2026, 11, 15).toEpochDay(), dateRanges.epochDayOf(noonIst))
    }

    @Test
    fun budgetKey_roundTrips() {
        val yearMonth = YearMonth.of(2026, 11)

        val key = yearMonth.toBudgetKey()

        assertEquals(202611, key)
        assertEquals(yearMonth, budgetKeyToYearMonth(key))
    }
}
