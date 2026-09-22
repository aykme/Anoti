package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CURRENT_ID = 11
private const val INCOMING_ID = 22
private const val CURRENT_ITEM_COUNT = 40
private const val INCOMING_ITEM_COUNT = 60

class SearchSectionReducerImplTest {

    private val reducer = SearchSectionReducerImpl()

    private fun listItem(id: AnimeId) = ListItemDomain(
        id = id,
        name = "Frieren $id",
        imageUrl = null,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 9.1F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    private val baseState = SearchSectionStore.State(
        searchText = "frieren",
        sectionContent = SectionContentDomain(
            contentType = ContentTypeDomain.LOADED,
            listItems = listOf(listItem(CURRENT_ID)),
            enabledExtraEpisodesInfoIds = setOf(CURRENT_ID),
            animeDetails = AnimeDetails(
                nextEpisodesInfo = mapOf(CURRENT_ID to "2026-01-05T18:00:00.000+03:00")
            )
        ),
        restoreTargetItemCount = CURRENT_ITEM_COUNT
    )

    private val incomingItems = listOf(listItem(INCOMING_ID))

    private val incomingDetails = AnimeDetails(
        nextEpisodesInfo = mapOf(INCOMING_ID to "2026-02-11T18:00:00.000+03:00")
    )

    private fun reduce(msg: SearchSectionStore.Message): SearchSectionStore.State {
        return with(reducer) { baseState.reduce(msg) }
    }

    @Test
    fun changeSearchTextReplacesOnlyTheSearchText() {
        //Given
        val msg = SearchSectionStore.Message.ChangeSearchText("bleach")

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(searchText = "bleach"), state)
    }

    @Test
    fun changeContentTypeReplacesOnlyTheContentType() {
        //Given
        val msg = SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(
                    contentType = ContentTypeDomain.ERROR
                )
            ),
            state
        )
    }

    @Test
    fun updateListItemsReplacesOnlyTheListItems() {
        //Given
        val msg = SearchSectionStore.Message.UpdateListItems(incomingItems)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(listItems = incomingItems)
            ),
            state
        )
    }

    @Test
    fun updateEnabledExtraEpisodesInfoIdsReplacesOnlyThatSet() {
        //Given
        val msg = SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(setOf(INCOMING_ID))

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(INCOMING_ID)
                )
            ),
            state
        )
    }

    @Test
    fun updateAnimeDetailsReplacesOnlyTheAnimeDetails() {
        //Given
        val msg = SearchSectionStore.Message.UpdateAnimeDetails(incomingDetails)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(animeDetails = incomingDetails)
            ),
            state
        )
    }

    @Test
    fun restoreSectionSeedsTheDisplayStateAndTheRestoreTargetWithoutTouchingTheQueryOrTheItems() {
        //Given
        val msg = SearchSectionStore.Message.RestoreSection(
            itemCount = INCOMING_ITEM_COUNT,
            enabledExtraEpisodesInfoIds = setOf(INCOMING_ID),
            nextEpisodesInfo = incomingDetails.nextEpisodesInfo
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(INCOMING_ID),
                    animeDetails = incomingDetails
                ),
                restoreTargetItemCount = INCOMING_ITEM_COUNT
            ),
            state
        )
    }

    @Test
    fun restoreSectionWithoutAnyLoadedItemsLeavesNoRestoreTarget() {
        //Given
        // A snapshot taken before the section ever loaded anything: opening it must page in the
        // first page as usual instead of trying to restore a position that does not exist.
        val msg = SearchSectionStore.Message.RestoreSection(
            itemCount = 0,
            enabledExtraEpisodesInfoIds = setOf(),
            nextEpisodesInfo = mapOf()
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(),
                    animeDetails = AnimeDetails()
                ),
                restoreTargetItemCount = null
            ),
            state
        )
    }

    @Test
    fun clearRestoreTargetItemCountClearsOnlyTheRestoreTarget() {
        //Given
        val msg = SearchSectionStore.Message.ClearRestoreTargetItemCount

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(restoreTargetItemCount = null), state)
    }
}
