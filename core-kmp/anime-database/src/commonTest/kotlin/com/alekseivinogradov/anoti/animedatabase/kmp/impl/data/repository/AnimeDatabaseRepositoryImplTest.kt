package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake.AnimeDaoFake
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimeDatabaseRepositoryImplTest {

    private fun sample(
        id: Int,
        isNewEpisode: Boolean = false,
        nextEpisodeAt: String? = null,
        isExtraInfoEnabled: Boolean = false
    ) = AnimeDbDomain(
        id = id,
        imageUrl = null,
        name = "Anime $id",
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = nextEpisodeAt,
        airedOn = null,
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = isNewEpisode,
        isExtraInfoEnabled = isExtraInfoEnabled
    )

    @Test
    fun insertThenGetAllItemsReturnsTheMappedDomainItem() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())

        //When
        repository.insert(sample(id = 1))

        //Then
        assertEquals(listOf(sample(id = 1)), repository.getAllItems())
    }

    @Test
    fun getAllItemsFlowEmitsAfterInsert() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())

        //When
        repository.insert(sample(id = 2))

        //Then
        assertEquals(listOf(sample(id = 2)), repository.getAllItemsFlow().first())
    }

    @Test
    fun updateReplacesTheExistingItem() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 3))

        //When
        repository.update(sample(id = 3, isNewEpisode = true))

        //Then
        assertTrue(repository.getAllItems().first().isNewEpisode)
    }

    @Test
    fun deleteRemovesTheItem() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 4))

        //When
        repository.delete(id = 4)

        //Then
        assertEquals(emptyList(), repository.getAllItems())
    }

    @Test
    fun resetAllItemsNewEpisodeStatusClearsEveryFlag() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 5, isNewEpisode = true))
        repository.insert(sample(id = 6, isNewEpisode = true))

        //When
        repository.resetAllItemsNewEpisodeStatus()

        //Then
        assertTrue(repository.getAllItems().none { it.isNewEpisode })
    }

    @Test
    fun changeItemNewEpisodeStatusUpdatesOnlyTheGivenId() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 7))
        repository.insert(sample(id = 8))

        //When
        repository.changeItemNewEpisodeStatus(id = 7, isNewEpisode = true)

        //Then
        val items = repository.getAllItems().associateBy { it.id }
        assertTrue(items.getValue(7).isNewEpisode)
        assertTrue(!items.getValue(8).isNewEpisode)
    }

    @Test
    fun resetAllItemsExtraInfoClearsEveryFlagAndNextEpisodeDate() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(
            sample(id = 9, nextEpisodeAt = "2026-09-10T12:00:00Z", isExtraInfoEnabled = true)
        )
        repository.insert(
            sample(id = 10, nextEpisodeAt = "2026-09-11T12:00:00Z", isExtraInfoEnabled = true)
        )

        //When
        repository.resetAllItemsExtraInfo()

        //Then
        val items = repository.getAllItems()
        assertTrue(items.none { it.isExtraInfoEnabled })
        assertTrue(items.none { it.nextEpisodeAt != null })
    }

    @Test
    fun getAllItemsFlowSkipsAnEmissionThatRepeatsTheCurrentList() = runTest(
        UnconfinedTestDispatcher()
    ) {
        //Given
        val dao = AnimeDaoFake()
        val repository = AnimeDatabaseRepositoryImpl(dao)
        val emitted = mutableListOf<List<AnimeDbDomain>>()
        backgroundScope.launch { repository.getAllItemsFlow().collect(emitted::add) }
        repository.insert(sample(id = 11))

        //When
        dao.republishStoredItems()
        dao.republishStoredItems()

        //Then
        assertEquals(listOf(emptyList(), listOf(sample(id = 11))), emitted)
    }

    @Test
    fun getAllItemsFlowEmitsEveryRealChange() = runTest(UnconfinedTestDispatcher()) {
        //Given
        val dao = AnimeDaoFake()
        val repository = AnimeDatabaseRepositoryImpl(dao)
        val emitted = mutableListOf<List<AnimeDbDomain>>()
        backgroundScope.launch { repository.getAllItemsFlow().collect(emitted::add) }

        //When
        repository.insert(sample(id = 12))
        repository.changeItemNewEpisodeStatus(id = 12, isNewEpisode = true)
        repository.delete(id = 12)

        //Then
        assertEquals(
            listOf(
                emptyList(),
                listOf(sample(id = 12)),
                listOf(sample(id = 12, isNewEpisode = true)),
                emptyList()
            ),
            emitted
        )
    }
}
