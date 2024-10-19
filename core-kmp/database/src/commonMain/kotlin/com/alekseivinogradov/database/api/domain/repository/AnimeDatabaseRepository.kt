package com.alekseivinogradov.database.api.domain.repository

import com.alekseivinogradov.database.api.domain.model.AnimeDb
import kotlinx.coroutines.flow.Flow

interface AnimeDatabaseRepository {
    suspend fun insert(anime: AnimeDb)

    suspend fun update(anime: AnimeDb)

    suspend fun getItem(id: Int): AnimeDb

    fun getAllItemsFlow(): Flow<List<AnimeDb>>

    suspend fun getAllItems(): List<AnimeDb>

    suspend fun delete(id: Int)

    suspend fun resetAllItemsNewEpisodeStatus()

    suspend fun changeItemNewEpisodeStatus(id: Int, isNewEpisode: Boolean)
}
