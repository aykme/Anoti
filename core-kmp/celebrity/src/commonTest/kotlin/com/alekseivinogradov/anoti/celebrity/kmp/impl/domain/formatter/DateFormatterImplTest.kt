package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter

import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatterImplTest {

    private val formatter = DateFormatterImpl()

    @Test
    fun formatsSingleDigitDayWithoutLeadingZero() {
        //Given
        val input = "2024-01-05"

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals("5 Jan 2024", formatted)
    }

    @Test
    fun formatsDoubleDigitDay() {
        //Given
        val input = "2024-12-25"

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals("25 Dec 2024", formatted)
    }

    @Test
    fun formatsDateTimeWithMillisecondsAndOffsetByTakingOnlyTheDatePart() {
        //Given
        val input = "2024-12-28T17:00:00.000+03:00"

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals("28 Dec 2024", formatted)
    }

    @Test
    fun formatsDateTimeWithOffsetByTakingOnlyTheDatePart() {
        //Given
        val input = "2024-12-28T17:00:00+03:00"

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals("28 Dec 2024", formatted)
    }

    @Test
    fun formatsDateTimeWithUtcSuffixByTakingOnlyTheDatePart() {
        //Given
        val input = "2026-08-16T12:00:00Z"

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals("16 Aug 2026", formatted)
    }

    @Test
    fun returnsFallbackForMalformedInput() {
        //Given
        val input = "not-a-date"

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals(FALLBACK, formatted)
    }

    @Test
    fun returnsFallbackForEmptyInput() {
        //Given
        val input = ""

        //When
        val formatted = formatter.getFormattedDate(input, FALLBACK)

        //Then
        assertEquals(FALLBACK, formatted)
    }

    @Test
    fun everyInstanceFormatsTheSameWay() {
        //Given
        val input = "2024-01-05"

        //When
        val formatted = DateFormatterImpl().getFormattedDate(input, FALLBACK)

        //Then
        assertEquals(formatter.getFormattedDate(input, FALLBACK), formatted)
    }

    private companion object {
        private const val FALLBACK = "fallback"
    }
}
