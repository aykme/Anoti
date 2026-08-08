package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

data class SectionContentDomain(
    val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
    val listItems: List<ListItemDomain> = emptyList(),
    val enabledExtraEpisodesInfoIds: Set<AnimeId> = setOf(),
    val animeDetails: AnimeDetails = AnimeDetails()
)
