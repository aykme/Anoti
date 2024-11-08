package com.alekseivinogradov.database.api.domain.repository

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import kotlinx.coroutines.flow.Flow

interface AnimeDatabaseRepository {
    suspend fun insert(anime: AnimeDbDomain)

    suspend fun update(anime: AnimeDbDomain)

    fun getAllItemsFlow(): Flow<List<AnimeDbDomain>>

    suspend fun getAllItems(): List<AnimeDbDomain>

    suspend fun delete(id: AnimeId)

    suspend fun resetAllItemsNewEpisodeStatus()

    suspend fun changeItemNewEpisodeStatus(id: Int, isNewEpisode: Boolean)
}
