package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePosterResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePostersDataResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePostersResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.GraphQlErrorResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.PosterResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/1/a.jpeg"

class AnimePostersResponseToPosterUrlsMapperTest {

    @Test
    fun eachPosterIsKeyedByItsNumericId() {
        //Given
        val response = postersOf(
            AnimePosterResponse(id = "2", poster = PosterResponse("$POSTER_URL?2")),
            AnimePosterResponse(id = "1", poster = PosterResponse(POSTER_URL))
        )

        //When
        val posterUrls = response.toPosterUrls()

        //Then
        assertEquals(mapOf(1 to POSTER_URL, 2 to "$POSTER_URL?2"), posterUrls)
    }

    @Test
    fun anEntryWithNoUsablePosterOrIdIsSkipped() {
        //Given
        val response = postersOf(
            AnimePosterResponse(id = "1", poster = null),
            AnimePosterResponse(id = "2", poster = PosterResponse(originalUrl = null)),
            // Neither platform allows cleartext: it would only show the error icon.
            AnimePosterResponse(id = "3", poster = PosterResponse("http://shikimori.io/a.jpeg")),
            AnimePosterResponse(id = "not-a-number", poster = PosterResponse(POSTER_URL)),
            AnimePosterResponse(id = "5", poster = PosterResponse(POSTER_URL))
        )

        //When
        val posterUrls = response.toPosterUrls()

        //Then
        assertEquals(mapOf(5 to POSTER_URL), posterUrls)
    }

    @Test
    fun dataThatComesWithErrorsIsStillUsed() {
        //Given
        val response = AnimePostersResponse(
            data = AnimePostersDataResponse(
                animes = listOf(AnimePosterResponse(id = "1", poster = PosterResponse(POSTER_URL)))
            ),
            errors = listOf(GraphQlErrorResponse(message = "partial"))
        )

        //When
        val posterUrls = response.toPosterUrls()

        //Then
        assertEquals(mapOf(1 to POSTER_URL), posterUrls)
    }

    @Test
    fun anAnswerWithoutDataIsAFailure() {
        //Given
        val response = AnimePostersResponse(errors = listOf(GraphQlErrorResponse("bad query")))

        //When / Then
        assertFailsWith<IllegalStateException> { response.toPosterUrls() }
    }

    private fun postersOf(vararg animes: AnimePosterResponse) =
        AnimePostersResponse(data = AnimePostersDataResponse(animes = animes.toList()))
}
