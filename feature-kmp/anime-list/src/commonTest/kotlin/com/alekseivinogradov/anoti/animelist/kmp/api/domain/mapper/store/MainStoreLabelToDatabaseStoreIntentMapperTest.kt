package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CLICKED_ID = 11

class MainStoreLabelToDatabaseStoreIntentMapperTest {

    private val listItem = ListItemDomain(
        id = CLICKED_ID,
        name = "Frieren",
        imageUrl = "https://example.com/frieren.jpg",
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = "2026-01-05T18:00:00.000+03:00",
        airedOn = "2023-09-29",
        releasedOn = "2024-03-22",
        score = 9.1F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    @Test
    fun enablingANotificationInsertsTheClickedItemIntoTheDatabase() {
        //Given
        val label = AnimeListMainStore.Label.EnableNotificationClick(listItem)

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                AnimeDbDomain(
                    id = CLICKED_ID,
                    imageUrl = "https://example.com/frieren.jpg",
                    name = "Frieren",
                    episodesAired = 7,
                    episodesTotal = 28,
                    nextEpisodeAt = "2026-01-05T18:00:00.000+03:00",
                    airedOn = "2023-09-29",
                    releasedOn = "2024-03-22",
                    score = 9.1F,
                    releaseStatus = ReleaseStatusDb.ONGOING,
                    episodesViewed = 0,
                    isNewEpisode = false
                )
            ),
            intent
        )
    }

    @Test
    fun disablingANotificationDeletesTheClickedItemFromTheDatabase() {
        //Given
        val label = AnimeListMainStore.Label.DisableNotificationClick(CLICKED_ID)

        //When
        val intent = mapMainStoreLabelToDatabaseStoreIntent(label)

        //Then
        assertEquals(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(CLICKED_ID), intent)
    }

    @Test
    fun labelsOfOtherConcernsProduceNoDatabaseIntent() {
        //Given
        val labels = listOf(
            AnimeListMainStore.Label.OpenOngoingSection,
            AnimeListMainStore.Label.UpdateOngoingSection,
            AnimeListMainStore.Label.LoadNextPageOngoingSection,
            AnimeListMainStore.Label.OngoingEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.OpenAnnouncedSection,
            AnimeListMainStore.Label.UpdateAnnouncedSection,
            AnimeListMainStore.Label.LoadNextPageAnnouncedSection,
            AnimeListMainStore.Label.AnnouncedEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.OpenSearchSection,
            AnimeListMainStore.Label.UpdateSearchSection,
            AnimeListMainStore.Label.LoadNextPageSearchSection,
            AnimeListMainStore.Label.SearchEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.ChangeSearchText("frieren")
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToDatabaseStoreIntent)

        //Then
        assertEquals(emptyList(), intents.filterNotNull())
    }
}
