package com.alekseivinogradov.anime_list.api.domain.model

import app.cash.paging.PagingData
import com.alekseivinogradov.celebrity.api.domain.AnimeId

data class SectionContentDomain(
    val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
    val listItems: PagingData<ListItemDomain> = PagingData.empty(),
    val enabledExtraEpisodesInfoIds: Set<AnimeId> = setOf(),
    val animeDetails: AnimeDetails = AnimeDetails()
)
