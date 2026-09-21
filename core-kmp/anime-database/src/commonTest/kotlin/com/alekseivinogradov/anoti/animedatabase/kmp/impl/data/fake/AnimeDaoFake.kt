package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake

import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDao
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * In-memory [AnimeDao].
 *
 * Its flow republishes the whole list on every write, the way Room's invalidation tracker does —
 * an unchanged list is published again rather than swallowed.
 *
 * @param beforeWrite runs at the start of every mutating call, so a test can hold a write open
 * or make it fail. Reassignable, so a test can arrange its database first and only then start
 * holding or failing writes.
 */
class AnimeDaoFake(
    var beforeWrite: suspend () -> Unit = {}
) : AnimeDao {
    private val items = MutableStateFlow<List<AnimeDbEntity>>(emptyList())

    private val emissions = MutableSharedFlow<List<AnimeDbEntity>>(
        replay = 1,
        extraBufferCapacity = EMISSION_BUFFER
    ).apply { tryEmit(emptyList()) }

    /** Number of collectors currently subscribed to [getAllItemsFlow]. */
    val subscriptionCount: StateFlow<Int> = emissions.subscriptionCount

    /** Publishes the stored list again unchanged, as Room does after a write that changed no row. */
    fun republishStoredItems() {
        emissions.tryEmit(items.value)
    }

    override suspend fun insert(anime: AnimeDbEntity) {
        beforeWrite()
        if (items.value.none { it.id == anime.id }) {
            items.value += anime
        }
        republishStoredItems()
    }

    override suspend fun update(anime: AnimeDbEntity) {
        beforeWrite()
        items.value = items.value.map { if (it.id == anime.id) anime else it }
        republishStoredItems()
    }

    override fun getAllItemsFlow(): Flow<List<AnimeDbEntity>> = emissions.asSharedFlow()

    override suspend fun getAllItems(): List<AnimeDbEntity> = items.value

    override suspend fun delete(id: AnimeId) {
        beforeWrite()
        items.value = items.value.filterNot { it.id == id }
        republishStoredItems()
    }

    override suspend fun resetAllItemsNewEpisodeStatus() {
        beforeWrite()
        items.value = items.value.map { it.copy(isNewEpisode = false) }
        republishStoredItems()
    }

    override suspend fun changeItemNewEpisodeStatus(id: AnimeId, isNewEpisode: Boolean) {
        beforeWrite()
        items.value = items.value.map {
            if (it.id == id) it.copy(isNewEpisode = isNewEpisode) else it
        }
        republishStoredItems()
    }

    override suspend fun resetAllItemsExtraInfo() {
        beforeWrite()
        items.value = items.value.map { it.copy(isExtraInfoEnabled = false, nextEpisodeAt = null) }
        republishStoredItems()
    }

    private companion object {
        private const val EMISSION_BUFFER = 64
    }
}
