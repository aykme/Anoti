package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter

/** Formats date strings for display. */
interface DateFormatter {
    /**
     * @param inputText the raw date string to format.
     * @param fallbackText returned as-is if [inputText] can't be parsed.
     */
    fun getFormattedDate(
        inputText: String,
        fallbackText: String,
    ): String
}
