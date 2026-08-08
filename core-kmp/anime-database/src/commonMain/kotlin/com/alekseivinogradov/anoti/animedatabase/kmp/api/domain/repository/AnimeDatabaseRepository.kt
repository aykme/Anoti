package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
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
