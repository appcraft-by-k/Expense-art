package com.appcraftbyk.expense.art.core.money

/**
 * Formats paise (minor units) as Indian-grouped rupees: `₹1,25,000` and `₹12.34`.
 *
 * Grouping is applied manually instead of relying on a `Locale("en", "IN")` number format so the
 * output is identical on every device and test run (see Plan §15). Paise are only shown when they
 * are non-zero, which matches how amounts are entered and displayed in the app.
 *
 * Negative amounts are formatted as `-₹1,25,000`; the sign is taken from the string form of the
 * value so `Long.MIN_VALUE` cannot overflow during negation.
 */
class IndianCurrencyFormatter {

    fun format(amountMinor: Long): String {
        val raw = amountMinor.toString()
        val negative = raw.startsWith('-')
        val digits = if (negative) raw.substring(1) else raw

        // Pad so that at least one rupee digit exists: "5" -> "0.05", "100" -> "1.00".
        val padded = digits.padStart(3, '0')
        val rupees = padded.dropLast(2)
        val paise = padded.takeLast(2)

        val grouped = groupIndian(rupees)
        val fraction = if (paise == "00") "" else ".$paise"
        val sign = if (negative) "-₹" else "₹"
        return sign + grouped + fraction
    }

    /** `12345678` -> `1,23,45,678` (last group of three, then groups of two). */
    private fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits

        val lastThree = digits.takeLast(3)
        var rest = digits.dropLast(3)
        val groups = ArrayDeque<String>()
        while (rest.length > 2) {
            groups.addFirst(rest.takeLast(2))
            rest = rest.dropLast(2)
        }
        if (rest.isNotEmpty()) groups.addFirst(rest)

        return groups.joinToString(separator = ",", postfix = ",$lastThree")
    }
}
