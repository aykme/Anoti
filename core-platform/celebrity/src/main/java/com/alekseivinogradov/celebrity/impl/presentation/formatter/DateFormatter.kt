package com.alekseivinogradov.celebrity.impl.presentation.formatter

import android.content.Context
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class DateFormatterImpl(
    activityContext: Context,
    isAutomaticLanguageDetection: Boolean
) : DateFormatter {

    private val locale = if (isAutomaticLanguageDetection) {
        activityContext.resources.configuration.locales.get(0) ?: Locale("en")
    } else {
        Locale("en")
    }

    private val inputDateParseFormatter = SimpleDateFormat(
        /* pattern = */ "yyyy-MM-dd",
        /* locale = */ locale
    )
    private val outputDateFormatter = SimpleDateFormat(
        /* pattern = */ "d MMM yyyy",
        /* locale = */ locale
    )

    override fun getFormattedDate(
        inputText: String,
        fallbackText: String,
    ): String {
        return try {
            val inputDate = inputDateParseFormatter.parse(inputText)
            outputDateFormatter.format(inputDate!!)
        } catch (e: ParseException) {
            fallbackText
        } catch (e: NullPointerException) {
            fallbackText
        }
    }
}
