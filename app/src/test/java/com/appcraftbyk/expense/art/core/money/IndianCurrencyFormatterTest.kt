package com.appcraftbyk.expense.art.core.money

import org.junit.Assert.assertEquals
import org.junit.Test

class IndianCurrencyFormatterTest {

    private val formatter = IndianCurrencyFormatter()

    @Test
    fun formatsZero() {
        assertEquals("₹0", formatter.format(0))
    }

    @Test
    fun formatsWholeRupees() {
        assertEquals("₹1", formatter.format(100))
        assertEquals("₹99", formatter.format(9_900))
    }

    @Test
    fun groupsThousandsInIndianStyle() {
        assertEquals("₹1,000", formatter.format(100_000))
        assertEquals("₹9,999", formatter.format(999_900))
        assertEquals("₹12,500", formatter.format(1_250_000))
        assertEquals("₹1,25,000", formatter.format(12_500_000))
        assertEquals("₹12,34,56,789", formatter.format(12_345_678_900))
    }

    @Test
    fun showsPaiseOnlyWhenPresent() {
        assertEquals("₹1.50", formatter.format(150))
        assertEquals("₹12.34", formatter.format(1_234))
        assertEquals("₹0.05", formatter.format(5))
        assertEquals("₹0.99", formatter.format(99))
    }

    @Test
    fun formatsNegativeAmounts() {
        assertEquals("-₹1", formatter.format(-100))
        assertEquals("-₹1,25,000", formatter.format(-12_500_000))
        assertEquals("-₹0.01", formatter.format(-1))
    }
}
