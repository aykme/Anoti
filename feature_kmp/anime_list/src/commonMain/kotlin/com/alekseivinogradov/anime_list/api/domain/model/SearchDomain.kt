package com.alekseivinogradov.anime_list.api.domain.model

internal data class SearchDomain(
    val type: Type = Type.HIDEN,
    val searchText: String = ""
) {
    enum class Type {
        HIDEN,
        SHOWN
    }
}