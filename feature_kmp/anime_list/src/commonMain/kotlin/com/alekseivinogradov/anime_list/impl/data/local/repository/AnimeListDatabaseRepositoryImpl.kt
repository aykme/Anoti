package com.alekseivinogradov.anime_list.impl.data.local.repository

import com.alekseivinogradov.anime_list.api.data.local.mapper.toDb
import com.alekseivinogradov.anime_list.api.data.local.mapper.toDomain
import com.alekseivinogradov.anime_list.api.data.local.repository.AnimeListDatabaseRepository
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.database.api.data.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.data.model.AnimeDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnimeListDatabaseRepositoryImpl(
    private val animeDatabaseRepository: AnimeDatabaseRepository
) : AnimeListDatabaseRepository {

    override suspend fun insert(anime: ListItemDomain) {
        animeDatabaseRepository.insert(anime.toDb())
    }

    override suspend fun delete(id: Int) {
        animeDatabaseRepository.delete(id)
    }

    override fun getAllItemsFlow(): Flow<List<ListItemDomain>> {
        return animeDatabaseRepository.getAllItemsFlow().map { animeDbList: List<AnimeDb> ->
            animeDbList.map { animeDb: AnimeDb ->
                animeDb.toDomain()
            }
        }
    }
}