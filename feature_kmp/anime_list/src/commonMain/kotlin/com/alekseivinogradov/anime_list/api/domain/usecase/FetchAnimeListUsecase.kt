package com.alekseivinogradov.anime_list.api.domain.usecase

import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.network.domain.model.CallResult

interface FetchAnimeListUsecase {

    suspend fun execute(
        page: Int,
        itemsPerPage: Int,
        searchText: String = ""
    ): CallResult<List<ListItemDomain>>
}