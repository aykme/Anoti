package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ONGOING_ID = 11
private const val ANNOUNCED_ID = 22
private const val SEARCH_ID = 33
private const val INCOMING_ID = 44

class AnimeListReducerImplTest {

    private val reducer = AnimeListReducerImpl()

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

    private fun sectionContent(id: AnimeId) = SectionContentDomain(
        contentType = ContentTypeDomain.LOADED,
        listItems = listOf(listItem(id)),
        enabledExtraEpisodesInfoIds = setOf(id),
        animeDetails = AnimeDetails(
            nextEpisodesInfo = mapOf(id to "2026-01-05T18:00:00.000+03:00")
        )
    )

    private val baseState = AnimeListMainStore.State(
        selectedSection = SectionHatDomain.ANNOUNCED,
        search = SearchDomain(type = SearchDomain.Type.SHOWN, searchText = "frieren"),
        ongoingContent = sectionContent(ONGOING_ID),
        announcedContent = sectionContent(ANNOUNCED_ID),
        searchContent = sectionContent(SEARCH_ID),
        enabledNotificationIds = setOf(ONGOING_ID),
        isNeedToResetListPositon = true
    )

    private val incomingItems = listOf(listItem(INCOMING_ID))

    private val incomingDetails = AnimeDetails(
        nextEpisodesInfo = mapOf(INCOMING_ID to "2026-02-11T18:00:00.000+03:00")
    )

    private fun reduce(msg: AnimeListMainStore.Message): AnimeListMainStore.State {
        return with(reducer) { baseState.reduce(msg) }
    }

    @Test
    fun changeSelectedSectionReplacesOnlyTheSelectedSection() {
        //Given
        val msg = AnimeListMainStore.Message.ChangeSelectedSection(SectionHatDomain.SEARCH)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(selectedSection = SectionHatDomain.SEARCH), state)
    }

    @Test
    fun changeSearchReplacesOnlyTheSearchBarState() {
        //Given
        val search = SearchDomain(type = SearchDomain.Type.HIDDEN, searchText = "")
        val msg = AnimeListMainStore.Message.ChangeSearch(search)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(search = search), state)
    }

    @Test
    fun changeResetListPositionFlagReplacesOnlyThatFlag() {
        //Given
        val msg = AnimeListMainStore.Message.ChangeResetListPositionFlag(
            isNeedToResetListPosition = false
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(isNeedToResetListPositon = false), state)
    }

    @Test
    fun changeOngoingContentTypeReplacesOnlyTheOngoingContentType() {
        //Given
        val msg = AnimeListMainStore.Message.ChangeOngoingContentType(ContentTypeDomain.ERROR)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                ongoingContent = baseState.ongoingContent.copy(
                    contentType = ContentTypeDomain.ERROR
                )
            ),
            state
        )
    }

    @Test
    fun updateOngoingListItemsReplacesOnlyTheOngoingListItems() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateOngoingListItems(incomingItems)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                ongoingContent = baseState.ongoingContent.copy(listItems = incomingItems)
            ),
            state
        )
    }

    @Test
    fun changeAnnouncedContentTypeReplacesOnlyTheAnnouncedContentType() {
        //Given
        val msg = AnimeListMainStore.Message.ChangeAnnouncedContentType(ContentTypeDomain.ERROR)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                announcedContent = baseState.announcedContent.copy(
                    contentType = ContentTypeDomain.ERROR
                )
            ),
            state
        )
    }

    @Test
    fun updateAnnouncedListItemsReplacesOnlyTheAnnouncedListItems() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateAnnouncedListItems(incomingItems)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                announcedContent = baseState.announcedContent.copy(listItems = incomingItems)
            ),
            state
        )
    }

    @Test
    fun changeSearchContentTypeReplacesOnlyTheSearchContentType() {
        //Given
        val msg = AnimeListMainStore.Message.ChangeSearchContentType(ContentTypeDomain.ERROR)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                searchContent = baseState.searchContent.copy(
                    contentType = ContentTypeDomain.ERROR
                )
            ),
            state
        )
    }

    @Test
    fun updateSearchListItemsReplacesOnlyTheSearchListItems() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateSearchListItems(incomingItems)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                searchContent = baseState.searchContent.copy(listItems = incomingItems)
            ),
            state
        )
    }

    @Test
    fun updateEnabledNotificationIdsReplacesOnlyThoseIds() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateEnabledNotificationIds(setOf(INCOMING_ID))

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(enabledNotificationIds = setOf(INCOMING_ID)), state)
    }

    @Test
    fun updateOngoingEnabledExtraEpisodesInfoIdsReplacesOnlyTheOngoingOnes() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateOngoingEnabledExtraEpisodesInfoIds(
            enabledExtraEpisodesInfoId = setOf(INCOMING_ID)
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                ongoingContent = baseState.ongoingContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(INCOMING_ID)
                )
            ),
            state
        )
    }

    @Test
    fun updateAnnouncedEnabledExtraEpisodesInfoIdsReplacesOnlyTheAnnouncedOnes() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateAnnouncedEnabledExtraEpisodesInfoIds(
            enabledExtraEpisodesInfoId = setOf(INCOMING_ID)
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                announcedContent = baseState.announcedContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(INCOMING_ID)
                )
            ),
            state
        )
    }

    @Test
    fun updateSearchEnabledExtraEpisodesInfoIdsReplacesOnlyTheSearchOnes() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateSearchEnabledExtraEpisodesInfoIds(
            enabledExtraEpisodesInfoId = setOf(INCOMING_ID)
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                searchContent = baseState.searchContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(INCOMING_ID)
                )
            ),
            state
        )
    }

    @Test
    fun updateOngoingAnimeDetailsReplacesOnlyTheOngoingAnimeDetails() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateOngoingAnimeDetails(incomingDetails)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                ongoingContent = baseState.ongoingContent.copy(animeDetails = incomingDetails)
            ),
            state
        )
    }

    @Test
    fun updateSearchAnimeDetailsReplacesOnlyTheSearchAnimeDetails() {
        //Given
        val msg = AnimeListMainStore.Message.UpdateSearchAnimeDetails(incomingDetails)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                searchContent = baseState.searchContent.copy(animeDetails = incomingDetails)
            ),
            state
        )
    }
}
