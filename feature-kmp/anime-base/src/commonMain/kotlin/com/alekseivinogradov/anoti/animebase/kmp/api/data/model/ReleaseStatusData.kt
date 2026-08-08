package com.alekseivinogradov.anoti.animebase.kmp.api.data.model

/** Release status values as sent/received by the Shikimori API. */
enum class ReleaseStatusData(val value: String) {
    ONGOING("ongoing"),
    ANNOUNCED("anons"),
    RELEASED("released")
}
