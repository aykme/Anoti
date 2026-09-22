package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ITEM_ID = 11

class SectionStoreStateToMainStoreIntentMapperTest {

    private val sectionContent = SectionContentDomain(
        contentType = ContentTypeDomain.LOADED,
        listItems = listOf(
            ListItemDomain(
                id = ITEM_ID,
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
        ),
        enabledExtraEpisodesInfoIds = setOf(ITEM_ID),
        animeDetails = AnimeDetails(
            nextEpisodesInfo = mapOf(ITEM_ID to "2026-01-05T18:00:00.000+03:00")
        )
    )

    @Test
    fun theOngoingSectionStateBecomesAnOngoingContentUpdate() {
        //Given
        val state = OngoingSectionStore.State(sectionContent = sectionContent)

        //When
        val intent = mapOngoingStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(AnimeListMainStore.Intent.UpdateOngoingContent(sectionContent), intent)
    }

    @Test
    fun theAnnouncedSectionStateBecomesAnAnnouncedContentUpdate() {
        //Given
        val state = AnnouncedSectionStore.State(sectionContent = sectionContent)

        //When
        val intent = mapAnnouncedStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(AnimeListMainStore.Intent.UpdateAnnouncedContent(sectionContent), intent)
    }

    @Test
    fun theSearchSectionStateBecomesASearchContentUpdate() {
        //Given
        val state = SearchSectionStore.State(sectionContent = sectionContent)

        //When
        val intent = mapSearchStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(AnimeListMainStore.Intent.UpdateSearchContent(sectionContent), intent)
    }
}
