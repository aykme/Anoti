package com.alekseivinogradov.anoti.animebase.kmp.api.data.model

/** Age rating filter values as sent to the Shikimori API. */
enum class RatingData(val value: String) {
    /** Every rating except `rx`, the sexually explicit one. */
    WITHOUT_EXPLICIT_CONTENT("!rx")
}
