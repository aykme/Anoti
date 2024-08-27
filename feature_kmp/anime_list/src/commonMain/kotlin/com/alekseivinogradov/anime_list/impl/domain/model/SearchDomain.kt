package com.alekseivinogradov.anime_list.impl.domain.model

internal data class SearchDomain(
    val type: SearchDomain.Type = Type.HIDEN,
    val searchText: String = ""
) {
    enum class Type {
        HIDEN,
        SHOWN
    }
}