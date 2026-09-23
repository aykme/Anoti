package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val ANIME_ID = 61316
private const val ORIGINAL_IMAGE_PATH = "/system/animes/original/61316.jpg"
private const val PAGE = 2
private const val TRANSPORT_FAILURE_MESSAGE = "no route to host"

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
class AnimeListSourceImplTest {

    private lateinit var lastRequest: HttpRequestData

    private val animeListJsonResponse = """
        [
          {
            "id": $ANIME_ID,
            "name": "Frieren",
            "russian": "Фрирен",
            "url": "/animes/$ANIME_ID",
            "image": { "original": "$ORIGINAL_IMAGE_PATH" },
            "episodes_aired": 7,
            "episodes": 28,
            "aired_on": "2025-10-01",
            "released_on": null,
            "score": 9.11,
            "status": "ongoing",
            "kind": "tv"
          }
        ]
    """.trimIndent()

    private val animeListWithAnEntryMissingItsId = """
        [
          { "name": "No id at all", "status": "ongoing" },
          {
            "id": $ANIME_ID,
            "name": "Frieren",
            "status": "ongoing"
          }
        ]
    """.trimIndent()

    private val animeDetailsJsonResponse = """
        {
          "id": $ANIME_ID,
          "name": "Frieren",
          "image": { "original": "$ORIGINAL_IMAGE_PATH" },
          "episodes_aired": 7,
          "episodes": 28,
          "next_episode_at": "2026-01-05T18:00:00.000+03:00",
          "aired_on": "2025-10-01",
          "score": 9.11,
          "status": "ongoing"
        }
    """.trimIndent()

    private val expectedListItem = ListItemDomain(
        id = ANIME_ID,
        name = "Frieren",
        imageUrl = SHIKIMORI_BASE_URL + ORIGINAL_IMAGE_PATH,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = "2025-10-01",
        releasedOn = null,
        score = 9.11F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    private fun createSource(jsonResponse: String): AnimeListSource {
        val engine = MockEngine { request ->
            lastRequest = request
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return AnimeListSourceImpl(
            service = ShikimoriApiServiceImpl(createHttpClient(engine)),
            safeApi = SafeApiFake()
        )
    }

    private fun createFailingSource(): AnimeListSource =
        AnimeListSourceImpl(
            service = ShikimoriApiServiceImpl(
                createHttpClient(
                    MockEngine { throw IllegalStateException(TRANSPORT_FAILURE_MESSAGE) }
                )
            ),
            safeApi = SafeApiFake()
        )

    @Test
    fun theOngoingListIsAskedForByItsReleaseStatusAndComesBackParsed() = runTest {
        //Given
        val source = createSource(animeListJsonResponse)

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
        assertEquals("ongoing", lastRequest.url.parameters["status"])
        assertEquals("popularity", lastRequest.url.parameters["order"])
        assertEquals(PAGE.toString(), lastRequest.url.parameters["page"])
        assertNull(lastRequest.url.parameters["search"])
    }

    @Test
    fun theAnnouncedListIsAskedForByItsOwnReleaseStatus() = runTest {
        //Given
        val source = createSource(animeListJsonResponse)

        //When
        source.getAnnouncedList(page = PAGE, sort = SortData.SCORE)

        //Then
        assertEquals("anons", lastRequest.url.parameters["status"])
        assertEquals("ranked", lastRequest.url.parameters["order"])
    }

    @Test
    fun aSearchIsSentAsAQueryWithNoReleaseStatus() = runTest {
        //Given
        val source = createSource(animeListJsonResponse)

        //When
        source.getListBySearch(page = PAGE, search = "frieren", sort = SortData.POPULARITY)

        //Then
        assertEquals("frieren", lastRequest.url.parameters["search"])
        assertNull(lastRequest.url.parameters["status"])
    }

    @Test
    fun anAnimeTheServerSendsWithoutAnIdIsLeftOutOfTheList() = runTest {
        //Given
        val source = createSource(animeListWithAnEntryMissingItsId)

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(ANIME_ID), result.value.map(ListItemDomain::id))
    }

    @Test
    fun oneAnimeIsAskedForByIdAndComesBackParsed() = runTest {
        //Given
        val source = createSource(animeDetailsJsonResponse)

        //When
        val result = source.getItemById(ANIME_ID)

        //Then
        assertEquals(
            CallResult.Success(
                expectedListItem.copy(nextEpisodeAt = "2026-01-05T18:00:00.000+03:00")
            ),
            result
        )
        assertTrue(lastRequest.url.encodedPath.endsWith("/api/animes/$ANIME_ID"))
    }

    @Test
    fun aListCallThatNeverReachesTheServerComesBackAsAnError() = runTest {
        //Given
        val source = createFailingSource()

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.OtherError)
        assertEquals(TRANSPORT_FAILURE_MESSAGE, result.throwable.message)
    }

    @Test
    fun aSingleAnimeCallThatNeverReachesTheServerComesBackAsAnError() = runTest {
        //Given
        val source = createFailingSource()

        //When
        val result = source.getItemById(ANIME_ID)

        //Then
        assertTrue(result is CallResult.OtherError)
        assertEquals(TRANSPORT_FAILURE_MESSAGE, result.throwable.message)
    }
}
