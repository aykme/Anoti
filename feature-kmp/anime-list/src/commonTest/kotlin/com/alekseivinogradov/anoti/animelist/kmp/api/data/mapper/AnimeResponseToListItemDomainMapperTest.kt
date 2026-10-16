package com.alekseivinogradov.anoti.animelist.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.IncompleteDateResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.PosterResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/21/a.jpeg"
private const val NEXT_EPISODE_AT = "2026-10-18T17:15:00+03:00"

class AnimeResponseToListItemDomainMapperTest {

    @Test
    fun everyFieldOfTheAnswerReachesTheListItem() {
        //Given
        val response = AnimeResponse(
            id = "21",
            englishName = "One Piece",
            poster = PosterResponse(originalUrl = POSTER_URL),
            episodesTotal = null,
            episodesAired = 1146,
            nextEpisodeAt = NEXT_EPISODE_AT,
            airedOn = IncompleteDateResponse(date = "1999-10-20"),
            releasedOn = IncompleteDateResponse(date = null),
            score = 8.73F,
            releaseStatus = "ongoing"
        )

        //When
        val listItem = response.toListItemDomain()

        //Then
        assertEquals(
            ListItemDomain(
                id = 21,
                name = "One Piece",
                imageUrl = POSTER_URL,
                episodesAired = 1146,
                episodesTotal = null,
                nextEpisodeAt = NEXT_EPISODE_AT,
                airedOn = "1999-10-20",
                releasedOn = null,
                score = 8.73F,
                releaseStatus = ReleaseStatusDomain.ONGOING
            ),
            listItem
        )
    }

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
    fun anAnimeWithoutAUsableIdIsNoListItem() {
        //Given
        val response = AnimeResponse(id = "z21", englishName = "One Piece")

        //When
        val listItem = response.toListItemDomain()

        //Then
        assertNull(listItem)
    }

    @Test
    fun aMissingDateObjectIsNoDate() {
        //Given
        val response = AnimeResponse(id = "21", airedOn = null, releasedOn = null)

        //When
        val listItem = response.toListItemDomain()

        //Then
        assertNull(listItem?.airedOn)
        assertNull(listItem?.releasedOn)
    }
}
