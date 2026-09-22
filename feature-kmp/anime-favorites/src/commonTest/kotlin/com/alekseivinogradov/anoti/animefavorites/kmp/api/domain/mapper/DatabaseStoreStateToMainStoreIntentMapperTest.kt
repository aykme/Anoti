package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseStoreStateToMainStoreIntentMapperTest {

    private val firstDbItem = AnimeDbDomain(
        id = 42,
        imageUrl = "https://example.org/cover.jpg",
        name = "First anime",
        episodesAired = 5,
        episodesTotal = 12,
        nextEpisodeAt = "2026-01-02",
        airedOn = "2025-10-01",
        releasedOn = "2026-03-01",
        score = 8.5F,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 3,
        isNewEpisode = true,
        isExtraInfoEnabled = true
    )

    private val secondDbItem = AnimeDbDomain(
        id = 7,
        imageUrl = null,
        name = "Second anime",
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDb.RELEASED,
        episodesViewed = 0,
        isNewEpisode = false,
        isExtraInfoEnabled = false
    )

    @Test
    fun everyDatabaseItemBecomesAListItemInTheOrderItArrived() {
        //Given
        val state = AnimeDatabaseStore.State(
            animeDatabaseItems = listOf(firstDbItem, secondDbItem)
        )

        //When
        val intent = mapDatabaseStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(
            AnimeFavoritesMainStore.Intent.UpdateListItems(
                listItems = listOf(
                    ListItemDomain(
                        id = 42,
                        name = "First anime",
                        imageUrl = "https://example.org/cover.jpg",
                        episodesAired = 5,
                        episodesTotal = 12,
                        nextEpisodeAt = "2026-01-02",
                        airedOn = "2025-10-01",
                        releasedOn = "2026-03-01",
                        score = 8.5F,
                        releaseStatus = ReleaseStatusDomain.ONGOING,
                        episodesViewed = 3,
                        isNewEpisode = true,
                        isExtraInfoEnabled = true
                    ),
                    ListItemDomain(
                        id = 7,
                        name = "Second anime",
                        imageUrl = null,
                        episodesAired = null,
                        episodesTotal = null,
                        nextEpisodeAt = null,
                        airedOn = null,
                        releasedOn = null,
                        score = null,
                        releaseStatus = ReleaseStatusDomain.RELEASED,
                        episodesViewed = 0,
                        isNewEpisode = false,
                        isExtraInfoEnabled = false
                    )
                )
            ),
            intent
        )
    }

    @Test
    fun anEmptyDatabaseStateBecomesAnIntentCarryingNoListItems() {
        //Given
        val state = AnimeDatabaseStore.State(animeDatabaseItems = listOf())

        //When
        val intent = mapDatabaseStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(AnimeFavoritesMainStore.Intent.UpdateListItems(listItems = listOf()), intent)
    }
}
