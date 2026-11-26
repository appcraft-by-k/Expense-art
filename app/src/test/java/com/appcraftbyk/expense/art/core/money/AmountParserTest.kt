package com.appcraftbyk.expense.art.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {

    @Test
    fun parsesWholeAndFractionalRupees() {
        assertEquals(5_000_000L, AmountParser.parseRupeesToMinor("50000"))
        assertEquals(5_000_050L, AmountParser.parseRupeesToMinor("50000.5"))
        assertEquals(5_000_050L, AmountParser.parseRupeesToMinor("50000.50"))
        assertEquals(5L, AmountParser.parseRupeesToMinor("0.05"))
        assertEquals(0L, AmountParser.parseRupeesToMinor("0"))
    }

    @Test
    fun ignoresCurrencySymbolCommasAndWhitespace() {
        assertEquals(125_000_00L, AmountParser.parseRupeesToMinor("₹1,25,000"))
        assertEquals(125_000_00L, AmountParser.parseRupeesToMinor(" 1,25,000 "))
    }

    @Test
    fun rejectsMalformedInput() {
        assertNull(AmountParser.parseRupeesToMinor(""))
        assertNull(AmountParser.parseRupeesToMinor("₹"))
        assertNull(AmountParser.parseRupeesToMinor("abc"))
        assertNull(AmountParser.parseRupeesToMinor("1.234"))
        assertNull(AmountParser.parseRupeesToMinor("-500"))
        assertNull(AmountParser.parseRupeesToMinor("1.2.3"))
        assertNull(AmountParser.parseRupeesToMinor("99999999999999999999"))
    }
}
