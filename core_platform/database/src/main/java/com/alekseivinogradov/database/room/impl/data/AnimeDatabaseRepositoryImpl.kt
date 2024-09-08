package com.alekseivinogradov.database.room.impl.data

import com.alekseivinogradov.database.api.data.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.room.api.data.AnimeDao
import com.alekseivinogradov.database.room.api.data.mapper.toKmp
import com.alekseivinogradov.database.room.api.data.mapper.toPlatform
import com.alekseivinogradov.database.room.api.data.model.AnimeDbPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnimeDatabaseRepositoryImpl(private val animeDao: AnimeDao) : AnimeDatabaseRepository {
    override suspend fun insert(anime: AnimeDb) {
        animeDao.insert(anime.toPlatform())
    }

    override suspend fun update(anime: AnimeDb) {
        animeDao.update(anime.toPlatform())
    }

    override suspend fun getItem(id: Int): AnimeDb {
        return animeDao.getItem(id).toKmp()
    }

    override fun getAllItemsFlow(): Flow<List<AnimeDb>> {
        return animeDao.getAllItemsFlow().map { animeDbPlatformList: List<AnimeDbPlatform> ->
            animeDbPlatformList.map { animeDbPlatform: AnimeDbPlatform ->
                animeDbPlatform.toKmp()
            }
        }
    }

    override suspend fun getAllItems(): List<AnimeDb> {
        return animeDao.getAllItems().map { animeDbPlatform: AnimeDbPlatform ->
            animeDbPlatform.toKmp()
        }
    }

    override suspend fun delete(id: Int) {
        animeDao.delete(id)
    }

    override suspend fun resetAllItemsNewEpisodeStatus() {
        animeDao.resetAllItemsNewEpisodeStatus()
    }
}