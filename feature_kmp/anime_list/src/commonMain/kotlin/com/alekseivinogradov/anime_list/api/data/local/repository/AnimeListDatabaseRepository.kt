package com.alekseivinogradov.anime_list.api.data.local.repository

import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import kotlinx.coroutines.flow.Flow

interface AnimeListDatabaseRepository {
    suspend fun insert(anime: ListItemDomain)

    suspend fun delete(id: Int)

    fun getAllItemsFlow(): Flow<List<ListItemDomain>>
}