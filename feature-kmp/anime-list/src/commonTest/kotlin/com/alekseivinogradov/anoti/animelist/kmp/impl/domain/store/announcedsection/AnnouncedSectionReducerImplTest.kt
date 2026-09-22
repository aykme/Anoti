package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CURRENT_ID = 11
private const val INCOMING_ID = 22
private const val CURRENT_ITEM_COUNT = 40
private const val INCOMING_ITEM_COUNT = 60

class AnnouncedSectionReducerImplTest {

    private val reducer = AnnouncedSectionReducerImpl()

    private fun listItem(id: AnimeId) = ListItemDomain(
        id = id,
        name = "Frieren $id",
        imageUrl = null,
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = "2026-09-01",
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDomain.ANNOUNCED
    )

    private val baseState = AnnouncedSectionStore.State(
        sectionContent = SectionContentDomain(
            contentType = ContentTypeDomain.LOADED,
            listItems = listOf(listItem(CURRENT_ID)),
            enabledExtraEpisodesInfoIds = setOf(CURRENT_ID)
        ),
        restoreTargetItemCount = CURRENT_ITEM_COUNT
    )

    private val incomingItems = listOf(listItem(INCOMING_ID))

    private fun reduce(msg: AnnouncedSectionStore.Message): AnnouncedSectionStore.State {
        return with(reducer) { baseState.reduce(msg) }
    }

    @Test
    fun changeContentTypeReplacesOnlyTheContentType() {
        //Given
        val msg = AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)

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
        val msg = AnnouncedSectionStore.Message.UpdateListItems(incomingItems)

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
        val msg =
            AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(setOf(INCOMING_ID))

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
    fun restoreSectionSeedsTheDisplayStateAndTheRestoreTargetWithoutTouchingTheItems() {
        //Given
        val msg = AnnouncedSectionStore.Message.RestoreSection(
            itemCount = INCOMING_ITEM_COUNT,
            enabledExtraEpisodesInfoIds = setOf(INCOMING_ID)
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(
                    enabledExtraEpisodesInfoIds = setOf(INCOMING_ID)
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
        val msg = AnnouncedSectionStore.Message.RestoreSection(
            itemCount = 0,
            enabledExtraEpisodesInfoIds = setOf()
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(
                sectionContent = baseState.sectionContent.copy(
                    enabledExtraEpisodesInfoIds = setOf()
                ),
                restoreTargetItemCount = null
            ),
            state
        )
    }

    @Test
    fun clearRestoreTargetItemCountClearsOnlyTheRestoreTarget() {
        //Given
        val msg = AnnouncedSectionStore.Message.ClearRestoreTargetItemCount

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(restoreTargetItemCount = null), state)
    }
}
