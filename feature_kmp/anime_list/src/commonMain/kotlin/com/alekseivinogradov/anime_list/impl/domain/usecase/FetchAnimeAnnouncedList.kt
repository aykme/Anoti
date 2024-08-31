package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.anime_network_base.api.data.model.SortData

class FetchAnimeAnnouncedList(
    private val source: AnimeListSource
) {

    suspend fun execute(page: Int, itemsPerPage: Int): List<ListItemDomain> {
        return source.getAnnouncedList(
            page = page,
            itemsPerPage = itemsPerPage,
            sort = SortData.POPULARITY.value
        )
    }
}