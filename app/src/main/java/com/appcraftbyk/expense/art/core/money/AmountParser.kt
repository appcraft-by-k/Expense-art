package com.appcraftbyk.expense.art.core.money

import java.math.BigDecimal

/**
 * Parses user-typed rupee amounts into minor units (paise).
 *
 * Accepts an optional `₹` prefix, Indian grouping commas and up to two decimals. Returns `null`
 * for anything else, including negative values and amounts that would overflow a `Long`, so
 * callers never have to guess whether a string was understood.
 */
object AmountParser {

    private val Rupees = Regex("""\d+(\.\d{1,2})?""")

    fun parseRupeesToMinor(input: String): Long? {
        val normalized = input
            .trim()
            .removePrefix("₹")
            .replace(",", "")
            .replace(" ", "")
        if (!Rupees.matches(normalized)) return null

        return try {
            BigDecimal(normalized).movePointRight(2).longValueExact()
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }
}
