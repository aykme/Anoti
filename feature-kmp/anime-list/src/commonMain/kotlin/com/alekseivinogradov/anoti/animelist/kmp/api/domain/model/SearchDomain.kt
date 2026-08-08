package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

/**
 * State of the search section's search bar.
 *
 * @param type whether the search bar is shown.
 * @param searchText current search text.
 */
data class SearchDomain(
    val type: Type = Type.HIDDEN,
    val searchText: String = ""
) {
    enum class Type {
        HIDDEN,
        SHOWN
    }
}
