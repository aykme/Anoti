package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter

/**
 * Hands every date back exactly as it came in, so a test asserts against the text it supplied
 * rather than against whatever the real formatter would produce for today's locale.
 */
class DateFormatterFake : DateFormatter {
    override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
}
