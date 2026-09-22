package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseStoreStateToMainStoreIntentMapperTest {

    private fun dbItem(id: AnimeId, isNewEpisode: Boolean) = AnimeDbDomain(
        id = id,
        imageUrl = null,
        name = "Frieren $id",
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 9.1F,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = isNewEpisode
    )

    private fun badgeNumberOf(vararg items: AnimeDbDomain): Int {
        val intent = mapDatabaseStoreStateToMainStoreIntent(
            AnimeDatabaseStore.State(animeDatabaseItems = items.toList())
        )
        return (intent as BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber)
            .favoritesBadgeNumber
    }

    @Test
    fun anEmptyDatabaseLeavesTheBadgeAtZero() {
        //Given
        val state = AnimeDatabaseStore.State()

        //When
        val intent = mapDatabaseStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(0), intent)
    }

    @Test
    fun onlyItemsWithANewEpisodeAreCounted() {
        //Given
        val withNewEpisode = dbItem(id = 1, isNewEpisode = true)
        val withoutNewEpisode = dbItem(id = 2, isNewEpisode = false)

        //When
        val badgeNumber = badgeNumberOf(withNewEpisode, withoutNewEpisode, withNewEpisode)

        //Then
        assertEquals(2, badgeNumber)
    }
}
