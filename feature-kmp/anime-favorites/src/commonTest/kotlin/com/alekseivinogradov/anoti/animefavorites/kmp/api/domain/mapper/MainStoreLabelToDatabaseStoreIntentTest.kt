package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import kotlin.test.Test
import kotlin.test.assertEquals

class MainStoreLabelToDatabaseStoreIntentTest {

    private val listItem = ListItemDomain(
        id = 42,
        name = "Anime",
        imageUrl = "https://example.org/cover.jpg",
        episodesAired = 5,
        episodesTotal = 12,
        nextEpisodeAt = "2026-01-02",
        airedOn = "2025-10-01",
        releasedOn = "2026-03-01",
        score = 8.5F,
        releaseStatus = ReleaseStatusDomain.RELEASED,
        episodesViewed = 4,
        isNewEpisode = true,
        isExtraInfoEnabled = true
    )

    @Test
    fun updateSectionAsksTheDatabaseToResetEveryNewEpisodeFlag() {
        //Given
        val label = AnimeFavoritesMainStore.Label.UpdateSection

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(AnimeDatabaseStore.Intent.ResetAllItemsNewEpisodeStatus, intent)
    }

    @Test
    fun resetExtraInfoAsksTheDatabaseToResetEveryItemsExtraInfo() {
        //Given
        val label = AnimeFavoritesMainStore.Label.ResetExtraInfo

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo, intent)
    }

    @Test
    fun itemClickClearsThatItemsNewEpisodeFlag() {
        //Given
        val label = AnimeFavoritesMainStore.Label.ItemClick(id = 42)

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(
            AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(isNewEpisode = false, id = 42),
            intent
        )
    }

    @Test
    fun disableNotificationClickDeletesThatItemFromTheDatabase() {
        //Given
        val label = AnimeFavoritesMainStore.Label.DisableNotificationClick(id = 42)

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(id = 42), intent)
    }

    @Test
    fun updateListItemPersistsThatItemAsItsDatabaseModel() {
        //Given
        val label = AnimeFavoritesMainStore.Label.UpdateListItem(listItem = listItem)

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(
            AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem(
                animeDatabaseItem = AnimeDbDomain(
                    id = 42,
                    imageUrl = "https://example.org/cover.jpg",
                    name = "Anime",
                    episodesAired = 5,
                    episodesTotal = 12,
                    nextEpisodeAt = "2026-01-02",
                    airedOn = "2025-10-01",
                    releasedOn = "2026-03-01",
                    score = 8.5F,
                    releaseStatus = ReleaseStatusDb.RELEASED,
                    episodesViewed = 4,
                    isNewEpisode = true,
                    isExtraInfoEnabled = true
                )
            ),
            intent
        )
    }
}
