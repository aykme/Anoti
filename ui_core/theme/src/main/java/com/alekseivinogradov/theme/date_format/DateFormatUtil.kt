package com.alekseivinogradov.theme.date_format

import android.content.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class DateFormatUtil private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: DateFormatUtil? = null

        fun getInstance(context: Context): DateFormatUtil {
            return instance ?: synchronized(this) {
                val newInstance = DateFormatUtil(context)
                instance = newInstance
                newInstance
            }
        }
    }

    private val locale = Locale("en")

    /** For automatic language detection */
//    private val local = context.applicationContext.resources.configuration.locales.get(0)
//        ?: Locale("en")

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