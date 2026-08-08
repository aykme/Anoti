package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter

interface DateFormatter {
    fun getFormattedDate(
        inputText: String,
        fallbackText: String,
    ): String
}
