package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * Parses ISO `yyyy-MM-dd` dates — or the date part of an ISO `yyyy-MM-ddTHH:mm:ss[...]`
 * date-time, e.g. the API's `next_episode_at` — and formats them as `d MMM yyyy`, e.g.
 * `5 Jan 2024`.
 */
class DateFormatterImpl : DateFormatter {

    private val outputFormat = LocalDate.Format {
        day(padding = Padding.NONE)
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        year()
    }

    override fun getFormattedDate(
        inputText: String,
        fallbackText: String,
    ): String {
        return try {
            val datePart = inputText.substringBefore('T')
            outputFormat.format(LocalDate.parse(datePart))
        } catch (_: IllegalArgumentException) {
            fallbackText
        }
    }
}
