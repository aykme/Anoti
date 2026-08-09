package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake

import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDao
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AnimeDaoFake : AnimeDao {
    private val items = MutableStateFlow<List<AnimeDbEntity>>(emptyList())

    override suspend fun insert(anime: AnimeDbEntity) {
        if (items.value.none { it.id == anime.id }) {
            items.value += anime
        }
    }

    override suspend fun update(anime: AnimeDbEntity) {
        items.value = items.value.map { if (it.id == anime.id) anime else it }
    }

    override fun getAllItemsFlow(): Flow<List<AnimeDbEntity>> = items

    override suspend fun getAllItems(): List<AnimeDbEntity> = items.value

    override suspend fun delete(id: AnimeId) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun resetAllItemsNewEpisodeStatus() {
        items.value = items.value.map { it.copy(isNewEpisode = false) }
    }

    override suspend fun changeItemNewEpisodeStatus(id: AnimeId, isNewEpisode: Boolean) {
        items.value = items.value.map {
            if (it.id == id) it.copy(isNewEpisode = isNewEpisode) else it
        }
    }
}
