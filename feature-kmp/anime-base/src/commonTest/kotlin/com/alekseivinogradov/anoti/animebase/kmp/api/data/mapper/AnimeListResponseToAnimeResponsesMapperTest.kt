package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListDataResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.GraphQlErrorResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

private const val ERROR_TEXT = "Variable \$search was provided invalid value frieren"

class AnimeListResponseToAnimeResponsesMapperTest {

    private val animes = listOf(AnimeResponse(id = "1"), AnimeResponse(id = "2"))

    @Test
    fun theAnimeOfTheAnswerComeBackInItsOrder() {
        //Given
        val response = AnimeListResponse(data = AnimeListDataResponse(animes = animes))

        //When
        val result = response.toAnimeResponses()

        //Then
        assertEquals(animes, result)
    }

    @Test
    fun dataThatComesWithErrorsIsStillUsed() {
        //Given
        val response = AnimeListResponse(
            data = AnimeListDataResponse(animes = animes),
            errors = listOf(GraphQlErrorResponse(message = ERROR_TEXT))
        )

        //When
        val result = response.toAnimeResponses()

        //Then
        assertEquals(animes, result)
    }

    @Test
    fun anAnswerWithoutDataFailsWithoutQuotingTheErrors() {
        //Given
        val response = AnimeListResponse(
            errors = listOf(GraphQlErrorResponse(message = ERROR_TEXT))
        )

        //When / Then
        val failure = assertFailsWith<IllegalStateException> { response.toAnimeResponses() }
        assertFalse(ERROR_TEXT in failure.message.orEmpty())
    }

    @Test
    fun dataWithoutItsAnimeFailsRatherThanReadingAsAnEmptyPage() {
        //Given
        // GraphQL answers this way when the field itself failed: an empty page would read as the
        // end of the list, or as every saved anime being gone.
        val response = AnimeListResponse(data = AnimeListDataResponse(animes = null))

        //When / Then
        assertFailsWith<IllegalStateException> { response.toAnimeResponses() }
    }
}
