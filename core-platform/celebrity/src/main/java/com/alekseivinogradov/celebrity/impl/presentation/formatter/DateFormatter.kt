package com.alekseivinogradov.celebrity.impl.presentation.formatter

import android.content.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class DateFormatter private constructor(
    context: Context,
    isAutomaticLanguageDetection: Boolean
) {

    companion object {
        @Volatile
        private var instance: DateFormatter? = null

        fun getInstance(
            context: Context,
            isAutomaticLanguageDetection: Boolean = false
        ): DateFormatter {
            return instance ?: synchronized(this) {
                val newInstance = DateFormatter(
                    context = context,
                    isAutomaticLanguageDetection = isAutomaticLanguageDetection
                )
                instance = newInstance
                newInstance
            }
        }
    }

    private val locale = if (isAutomaticLanguageDetection) {
        context.resources.configuration.locales.get(0) ?: Locale("en")
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

    fun getFormattedDate(
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
