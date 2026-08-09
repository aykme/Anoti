package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter

/** Formats date strings for display. */
interface DateFormatter {
    /**
     * @param inputText the raw date string to format — an ISO `yyyy-MM-dd` date, or an ISO
     * date-time (`yyyy-MM-ddTHH:mm:ss[...]`) whose date part is used and the rest ignored.
     * @param fallbackText returned as-is if [inputText] can't be parsed.
     */
    fun getFormattedDate(
        inputText: String,
        fallbackText: String,
    ): String
}
