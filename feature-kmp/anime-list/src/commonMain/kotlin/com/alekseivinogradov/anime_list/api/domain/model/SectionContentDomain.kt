package com.alekseivinogradov.anime_list.api.domain.model

import com.alekseivinogradov.celebrity.api.domain.AnimeId

data class SectionContentDomain(
    val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
    val listItems: List<ListItemDomain> = emptyList(),
    val enabledExtraEpisodesInfoIds: Set<AnimeId> = setOf(),
    val animeDetails: AnimeDetails = AnimeDetails()
)
