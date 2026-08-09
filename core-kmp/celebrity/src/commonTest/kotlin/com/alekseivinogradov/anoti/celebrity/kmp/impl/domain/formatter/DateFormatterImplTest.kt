package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter

import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatterImplTest {

    private val formatter = DateFormatterImpl()

    @Test
    fun formatsSingleDigitDayWithoutLeadingZero() {
        assertEquals("5 Jan 2024", formatter.getFormattedDate("2024-01-05", "fallback"))
    }

    @Test
    fun formatsDoubleDigitDay() {
        assertEquals("25 Dec 2024", formatter.getFormattedDate("2024-12-25", "fallback"))
    }

    @Test
    fun formatsDateTimeWithMillisecondsAndOffsetByTakingOnlyTheDatePart() {
        assertEquals(
            "28 Dec 2024",
            formatter.getFormattedDate("2024-12-28T17:00:00.000+03:00", "fallback")
        )
    }

    @Test
    fun formatsDateTimeWithOffsetByTakingOnlyTheDatePart() {
        assertEquals(
            "28 Dec 2024",
            formatter.getFormattedDate("2024-12-28T17:00:00+03:00", "fallback")
        )
    }

    @Test
    fun formatsDateTimeWithUtcSuffixByTakingOnlyTheDatePart() {
        assertEquals(
            "16 Aug 2026",
            formatter.getFormattedDate("2026-08-16T12:00:00Z", "fallback")
        )
    }

    @Test
    fun returnsFallbackForMalformedInput() {
        assertEquals("fallback", formatter.getFormattedDate("not-a-date", "fallback"))
    }

    @Test
    fun returnsFallbackForEmptyInput() {
        assertEquals("fallback", formatter.getFormattedDate("", "fallback"))
    }
}
