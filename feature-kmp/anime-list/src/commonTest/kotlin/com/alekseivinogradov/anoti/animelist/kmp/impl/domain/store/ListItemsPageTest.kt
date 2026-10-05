package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

class ListItemsPageTest {

    private fun item(id: AnimeId, name: String = "Anime $id") = ListItemDomain(
        id = id,
        name = name,
        imageUrl = null,
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 8.0F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    @Test
    fun aPageOfNewItemsIsAppendedInOrder() {
        //Given
        val loaded = listOf(item(id = 1), item(id = 2))

        //When
        val merged = loaded.plusPage(listOf(item(id = 3), item(id = 4)))

        //Then
        assertEquals(listOf(1, 2, 3, 4), merged.map { it.id })
    }

    @Test
    fun anItemAlreadyLoadedKeepsItsFirstPlaceAndData() {
        //Given
        val loaded = listOf(item(id = 1, name = "first copy"), item(id = 2))

        //When
        val merged = loaded.plusPage(listOf(item(id = 1, name = "second copy"), item(id = 3)))

        //Then
        assertEquals(listOf(1, 2, 3), merged.map { it.id })
        assertEquals("first copy", merged.first().name)
    }

    @Test
    fun anItemRepeatedWithinOnePageIsKeptOnce() {
        //Given
        val loaded = emptyList<ListItemDomain>()

        //When
        val merged = loaded.plusPage(listOf(item(id = 5), item(id = 5), item(id = 6)))

        //Then
        assertEquals(listOf(5, 6), merged.map { it.id })
    }
}
