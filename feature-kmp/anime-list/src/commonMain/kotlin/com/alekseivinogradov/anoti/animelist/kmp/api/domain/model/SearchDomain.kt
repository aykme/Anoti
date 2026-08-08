package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

data class SearchDomain(
    val type: Type = Type.HIDDEN,
    val searchText: String = ""
) {
    enum class Type {
        HIDDEN,
        SHOWN
    }
}
