package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDao
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.mapper.toDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.mapper.toDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnimeDatabaseRepositoryImpl(private val animeDao: AnimeDao) : AnimeDatabaseRepository {
    override suspend fun insert(anime: AnimeDbDomain) {
        animeDao.insert(anime.toDb())
    }

    override suspend fun update(anime: AnimeDbDomain) {
        animeDao.update(anime.toDb())
    }

    override fun getAllItemsFlow(): Flow<List<AnimeDbDomain>> {
        return animeDao.getAllItemsFlow().map { entities: List<AnimeDbEntity> ->
            entities.map { entity: AnimeDbEntity -> entity.toDomain() }
        }
    }

    override suspend fun getAllItems(): List<AnimeDbDomain> {
        return animeDao.getAllItems().map { entity: AnimeDbEntity -> entity.toDomain() }
    }

    override suspend fun delete(id: AnimeId) {
        animeDao.delete(id)
    }

    override suspend fun resetAllItemsNewEpisodeStatus() {
        animeDao.resetAllItemsNewEpisodeStatus()
    }

    override suspend fun changeItemNewEpisodeStatus(id: AnimeId, isNewEpisode: Boolean) {
        animeDao.changeItemNewEpisodeStatus(id = id, isNewEpisode = isNewEpisode)
    }
}
