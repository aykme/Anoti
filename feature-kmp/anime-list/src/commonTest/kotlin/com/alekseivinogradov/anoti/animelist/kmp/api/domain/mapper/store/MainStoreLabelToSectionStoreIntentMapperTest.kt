package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CLICKED_ID = 11

class MainStoreLabelToSectionStoreIntentMapperTest {

    private val listItem = ListItemDomain(
        id = CLICKED_ID,
        name = "Frieren",
        imageUrl = null,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 9.1F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    @Test
    fun ongoingLabelsMapToTheirOngoingSectionIntents() {
        //Given
        val labels = listOf(
            AnimeListMainStore.Label.OpenOngoingSection,
            AnimeListMainStore.Label.UpdateOngoingSection,
            AnimeListMainStore.Label.OngoingEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.LoadNextPageOngoingSection
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToOngoingStoreIntent)

        //Then
        assertEquals(
            listOf(
                OngoingSectionStore.Intent.OpenSection,
                OngoingSectionStore.Intent.UpdateSection,
                OngoingSectionStore.Intent.EpisodesInfoClick(CLICKED_ID),
                OngoingSectionStore.Intent.LoadNextPage
            ),
            intents
        )
    }

    @Test
    fun labelsOfOtherConcernsProduceNoOngoingSectionIntent() {
        //Given
        val labels = listOf(
            AnimeListMainStore.Label.OpenAnnouncedSection,
            AnimeListMainStore.Label.UpdateAnnouncedSection,
            AnimeListMainStore.Label.LoadNextPageAnnouncedSection,
            AnimeListMainStore.Label.AnnouncedEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.OpenSearchSection,
            AnimeListMainStore.Label.UpdateSearchSection,
            AnimeListMainStore.Label.LoadNextPageSearchSection,
            AnimeListMainStore.Label.SearchEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.ChangeSearchText("frieren"),
            AnimeListMainStore.Label.EnableNotificationClick(listItem),
            AnimeListMainStore.Label.DisableNotificationClick(CLICKED_ID)
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToOngoingStoreIntent)

        //Then
        assertEquals(emptyList(), intents.filterNotNull())
    }

    @Test
    fun announcedLabelsMapToTheirAnnouncedSectionIntents() {
        //Given
        val labels = listOf(
            AnimeListMainStore.Label.OpenAnnouncedSection,
            AnimeListMainStore.Label.UpdateAnnouncedSection,
            AnimeListMainStore.Label.AnnouncedEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.LoadNextPageAnnouncedSection
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToAnnouncedStoreIntent)

        //Then
        assertEquals(
            listOf(
                AnnouncedSectionStore.Intent.OpenSection,
                AnnouncedSectionStore.Intent.UpdateSection,
                AnnouncedSectionStore.Intent.EpisodesInfoClick(CLICKED_ID),
                AnnouncedSectionStore.Intent.LoadNextPage
            ),
            intents
        )
    }

    @Test
    fun labelsOfOtherConcernsProduceNoAnnouncedSectionIntent() {
        //Given
        val labels = listOf(
            AnimeListMainStore.Label.OpenOngoingSection,
            AnimeListMainStore.Label.UpdateOngoingSection,
            AnimeListMainStore.Label.LoadNextPageOngoingSection,
            AnimeListMainStore.Label.OngoingEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.OpenSearchSection,
            AnimeListMainStore.Label.UpdateSearchSection,
            AnimeListMainStore.Label.LoadNextPageSearchSection,
            AnimeListMainStore.Label.SearchEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.ChangeSearchText("frieren"),
            AnimeListMainStore.Label.EnableNotificationClick(listItem),
            AnimeListMainStore.Label.DisableNotificationClick(CLICKED_ID)
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToAnnouncedStoreIntent)

        //Then
        assertEquals(emptyList(), intents.filterNotNull())
    }

    @Test
    fun searchLabelsMapToTheirSearchSectionIntents() {
        //Given
        val labels = listOf(
            AnimeListMainStore.Label.OpenSearchSection,
            AnimeListMainStore.Label.UpdateSearchSection,
            AnimeListMainStore.Label.SearchEpisodeInfoClick(CLICKED_ID),
            AnimeListMainStore.Label.ChangeSearchText("frieren"),
            AnimeListMainStore.Label.LoadNextPageSearchSection
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToSearchStoreIntent)

        //Then
        assertEquals(
            listOf(
                SearchSectionStore.Intent.OpenSection,
                SearchSectionStore.Intent.UpdateSection,
                SearchSectionStore.Intent.EpisodesInfoClick(CLICKED_ID),
                SearchSectionStore.Intent.ChangeSearchText("frieren"),
                SearchSectionStore.Intent.LoadNextPage
            ),
            intents
        )
    }

    @Test
    fun labelsOfOtherConcernsProduceNoSearchSectionIntent() {
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
            AnimeListMainStore.Label.EnableNotificationClick(listItem),
            AnimeListMainStore.Label.DisableNotificationClick(CLICKED_ID)
        )

        //When
        val intents = labels.map(::mapMainStoreLabelToSearchStoreIntent)

        //Then
        assertEquals(emptyList(), intents.filterNotNull())
    }
}
