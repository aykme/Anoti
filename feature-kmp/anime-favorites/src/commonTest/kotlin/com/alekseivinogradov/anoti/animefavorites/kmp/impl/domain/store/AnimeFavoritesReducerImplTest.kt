package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CURRENT_ID = 11
private const val INCOMING_ID = 22

class AnimeFavoritesReducerImplTest {

    private val reducer = AnimeFavoritesReducerImpl()

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
        releaseStatus = ReleaseStatusDomain.ONGOING,
        episodesViewed = 3,
        isNewEpisode = false,
        isExtraInfoEnabled = true
    )

    private val baseState = AnimeFavoritesMainStore.State(
        listItems = listOf(listItem(CURRENT_ID)),
        contentType = ContentTypeDomain.LOADED,
        fetchedAnimeDetailsIds = setOf(CURRENT_ID)
    )

    private fun reduce(msg: AnimeFavoritesMainStore.Message): AnimeFavoritesMainStore.State {
        return with(reducer) { baseState.reduce(msg) }
    }

    @Test
    fun updateListItemsReplacesOnlyTheListItems() {
        //Given
        val incomingItems = listOf(listItem(INCOMING_ID))
        val msg = AnimeFavoritesMainStore.Message.UpdateListItems(incomingItems)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(listItems = incomingItems), state)
    }

    @Test
    fun changeContentTypeReplacesOnlyTheContentType() {
        //Given
        // The fetched ids must survive it: they are what stops an already-answered item being
        // asked for a second time.
        val msg = AnimeFavoritesMainStore.Message.ChangeContentType(
            ContentTypeDomain.LOADING(hasMinimumDuration = true)
        )

        //When
        val state = reduce(msg)

        //Then
        assertEquals(
            baseState.copy(contentType = ContentTypeDomain.LOADING(hasMinimumDuration = true)),
            state
        )
    }

    @Test
    fun updateFetchedAnimeDetailsIdsReplacesOnlyThatSet() {
        //Given
        val msg = AnimeFavoritesMainStore.Message.UpdateFetchedAnimeDetailsIds(setOf(INCOMING_ID))

        //When
        val state = reduce(msg)

        //Then
        assertEquals(baseState.copy(fetchedAnimeDetailsIds = setOf(INCOMING_ID)), state)
    }
}
