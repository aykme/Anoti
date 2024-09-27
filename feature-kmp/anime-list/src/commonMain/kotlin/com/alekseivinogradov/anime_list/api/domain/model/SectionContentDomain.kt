package com.alekseivinogradov.anime_list.api.domain.model

import com.alekseivinogradov.anime_base.api.domain.AnimeId

data class SectionContentDomain(
    val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
    val listItems: List<ListItemDomain> = listOf(),
    val enabledExtraEpisodesInfoIds: Set<AnimeId> = setOf()
)
