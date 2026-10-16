package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.IncompleteDateResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnimeResponseToItemDomainMapperTest {

    @Test
    fun anAnimeWithoutAPosterHasNoImage() {
        //Given
        val response = AnimeResponse(id = "39169", poster = null)

        //When
        val listItem = response.toListItemDomain()

        //Then
        assertNull(listItem?.imageUrl)
    }

    @Test
    fun theDatesAreReadFromTheirObjects() {
        //Given
        val response = AnimeResponse(
            id = "21",
            airedOn = IncompleteDateResponse(date = "1999-10-20"),
            releasedOn = IncompleteDateResponse(date = null)
        )

        //When
        val listItem = response.toListItemDomain()

        //Then
        assertEquals("1999-10-20", listItem?.airedOn)
        assertNull(listItem?.releasedOn)
    }

    @Test
    fun anAnimeWithoutAUsableIdIsNoListItem() {
        //Given
        val response = AnimeResponse(id = null)

        //When
        val listItem = response.toListItemDomain()

        //Then
        assertNull(listItem)
    }
}
