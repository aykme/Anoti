package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake.AnimeDaoFake
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository.AnimeDatabaseRepositoryImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The one-shot read the background update pass uses. Nothing else in this module reaches it, so
 * it is driven directly here rather than through the store.
 */
class FetchAllAnimeDatabaseItemsUsecaseImplTest {

    private fun sample(id: Int) = AnimeDbDomain(
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
        isNewEpisode = false
    )

    @Test
    fun executeReturnsEverySavedItem() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        repository.insert(sample(id = 1))
        repository.insert(sample(id = 2))
        val usecase = FetchAllAnimeDatabaseItemsUsecaseImpl(repository)

        //When
        val items = usecase.execute()

        //Then
        assertEquals(listOf(sample(id = 1), sample(id = 2)), items)
    }

    @Test
    fun executeReturnsNothingWhenNoAnimeIsSaved() = runTest {
        //Given
        val repository = AnimeDatabaseRepositoryImpl(AnimeDaoFake())
        val usecase = FetchAllAnimeDatabaseItemsUsecaseImpl(repository)

        //When
        val items = usecase.execute()

        //Then
        assertEquals(emptyList(), items)
    }
}
