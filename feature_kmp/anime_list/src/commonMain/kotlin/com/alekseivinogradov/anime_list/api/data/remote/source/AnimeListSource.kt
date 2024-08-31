package com.alekseivinogradov.anime_list.api.data.remote.source

import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain

interface AnimeListSource {

    suspend fun getOngoingList(
        page: Int,
        itemsPerPage: Int,
        sort: String
    ): List<ListItemDomain>

    suspend fun getAnnouncedList(
        page: Int,
        itemsPerPage: Int,
        sort: String
    ): List<ListItemDomain>

    suspend fun getListBySearch(
        page: Int,
        itemsPerPage: Int,
        search: String,
        sort: String
    ): List<ListItemDomain>
}