package com.alekseivinogradov.anoti.animedatabase.platform.impl.data.repository

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.AnimeDao
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.mapper.toKmp
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.mapper.toPlatform
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.model.AnimeDbPlatform
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnimeDatabaseRepositoryImpl(private val animeDao: AnimeDao) : AnimeDatabaseRepository {
    override suspend fun insert(anime: AnimeDbDomain) {
        animeDao.insert(anime.toPlatform())
    }

    override suspend fun update(anime: AnimeDbDomain) {
        animeDao.update(anime.toPlatform())
    }

    override fun getAllItemsFlow(): Flow<List<AnimeDbDomain>> {
        return animeDao.getAllItemsFlow().map { animeDbPlatformList: List<AnimeDbPlatform> ->
            animeDbPlatformList.map { animeDbPlatform: AnimeDbPlatform ->
                animeDbPlatform.toKmp()
            }
        }
    }

    override suspend fun getAllItems(): List<AnimeDbDomain> {
        return animeDao.getAllItems().map { animeDbPlatform: AnimeDbPlatform ->
            animeDbPlatform.toKmp()
        }
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
