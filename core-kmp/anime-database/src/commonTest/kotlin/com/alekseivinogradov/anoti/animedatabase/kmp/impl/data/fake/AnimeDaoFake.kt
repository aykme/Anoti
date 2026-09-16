package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake

import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDao
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory [AnimeDao].
 *
 * @param beforeWrite runs at the start of every mutating call, so a test can hold a write open
 * or make it fail.
 */
class AnimeDaoFake(
    private val beforeWrite: suspend () -> Unit = {}
) : AnimeDao {
    private val items = MutableStateFlow<List<AnimeDbEntity>>(emptyList())

    /** Number of collectors currently subscribed to [getAllItemsFlow]. */
    val subscriptionCount: StateFlow<Int> = items.subscriptionCount

    override suspend fun insert(anime: AnimeDbEntity) {
        beforeWrite()
        if (items.value.none { it.id == anime.id }) {
            items.value += anime
        }
    }

    override suspend fun update(anime: AnimeDbEntity) {
        beforeWrite()
        items.value = items.value.map { if (it.id == anime.id) anime else it }
    }

    override fun getAllItemsFlow(): Flow<List<AnimeDbEntity>> = items

    override suspend fun getAllItems(): List<AnimeDbEntity> = items.value

    override suspend fun delete(id: AnimeId) {
        beforeWrite()
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun resetAllItemsNewEpisodeStatus() {
        beforeWrite()
        items.value = items.value.map { it.copy(isNewEpisode = false) }
    }

    override suspend fun changeItemNewEpisodeStatus(id: AnimeId, isNewEpisode: Boolean) {
        beforeWrite()
        items.value = items.value.map {
            if (it.id == id) it.copy(isNewEpisode = isNewEpisode) else it
        }
    }

    override suspend fun resetAllItemsExtraInfo() {
        beforeWrite()
        items.value = items.value.map { it.copy(isExtraInfoEnabled = false, nextEpisodeAt = null) }
    }
}
