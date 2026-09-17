package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster

import kotlin.test.Test
import kotlin.test.assertEquals

class PosterFileNameTest {

    @Test
    fun fileNameKeepsTheExtensionAndDropsTheQuery() {
        //Given
        val imageUrl = "https://shikimori.io/system/animes/original/61316.jpg?1757000000"

        //When
        val fileName = posterFileName(imageUrl)

        //Then
        assertEquals("anime_notification_poster_61316.jpg", fileName)
    }

    @Test
    fun fileNameOfUrlWithoutQueryIsItsLastSegment() {
        //Given
        val imageUrl = "https://shikimori.io/assets/globals/missing_original.jpg"

        //When
        val fileName = posterFileName(imageUrl)

        //Then
        assertEquals("anime_notification_poster_missing_original.jpg", fileName)
    }
}
