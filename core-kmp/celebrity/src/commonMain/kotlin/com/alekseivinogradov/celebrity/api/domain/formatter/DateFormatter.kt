package com.alekseivinogradov.celebrity.api.domain.formatter

interface DateFormatter {
    fun getFormattedDate(
        inputText: String,
        fallbackText: String,
    ): String
}
