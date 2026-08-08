package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake.AnimeDaoFake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class AnimeDatabaseRepositoryImplTest {

    private fun sample(id: Int, isNewEpisode: Boolean = false) = AnimeDbDomain(
        id = id,
        imageUrl = null,
        name = "Anime $id",
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = isNewEpisode
    )

    @Test
    fun insertThenGetAllItemsReturnsTheMappedDomainItem() = runTest {
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())

        repository.insert(sample(id = 1))

        assertEquals(listOf(sample(id = 1)), repository.getAllItems())
    }

    @Test
    fun getAllItemsFlowEmitsAfterInsert() = runTest {
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())

        repository.insert(sample(id = 2))

        assertEquals(listOf(sample(id = 2)), repository.getAllItemsFlow().first())
    }

    @Test
    fun updateReplacesTheExistingItem() = runTest {
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 3))

        repository.update(sample(id = 3, isNewEpisode = true))

        assertTrue(repository.getAllItems().first().isNewEpisode)
    }

    @Test
    fun deleteRemovesTheItem() = runTest {
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 4))

        repository.delete(id = 4)

        assertEquals(emptyList(), repository.getAllItems())
    }

    @Test
    fun resetAllItemsNewEpisodeStatusClearsEveryFlag() = runTest {
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 5, isNewEpisode = true))
        repository.insert(sample(id = 6, isNewEpisode = true))

        repository.resetAllItemsNewEpisodeStatus()

        assertTrue(repository.getAllItems().none { it.isNewEpisode })
    }

    @Test
    fun changeItemNewEpisodeStatusUpdatesOnlyTheGivenId() = runTest {
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 7))
        repository.insert(sample(id = 8))

        repository.changeItemNewEpisodeStatus(id = 7, isNewEpisode = true)

        val items = repository.getAllItems().associateBy { it.id }
        assertTrue(items.getValue(7).isNewEpisode)
        assertTrue(!items.getValue(8).isNewEpisode)
    }
}
