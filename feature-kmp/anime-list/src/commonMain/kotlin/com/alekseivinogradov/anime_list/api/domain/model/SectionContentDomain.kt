package com.alekseivinogradov.anime_list.api.domain.model

data class SectionContentDomain(
    val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
    val listItems: List<ListItemDomain> = listOf()
)
