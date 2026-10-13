package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.GRAPHQL_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.source.AnimePosterSourceImpl
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val ANIME_ID = 61316
private const val ORIGINAL_IMAGE_PATH = "/system/animes/original/61316.jpg"
private const val PAGE = 2
private const val TRANSPORT_FAILURE_MESSAGE = "no route to host"
private const val OTHER_ANIME_ID = 52991
private const val THIRD_ANIME_ID = 5114
private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/61316/a.jpeg"
private const val NO_POSTERS_JSON = """{"data":{"animes":[]}}"""

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class AnimeListSourceImplTest {

    private val restRequests = mutableListOf<HttpRequestData>()
    private val graphQlRequests = mutableListOf<HttpRequestData>()

    private val frierenJson = """
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
    """.trimIndent()

    private val animeListJsonResponse = "[$frierenJson]"

    private val twoAnimeJsonResponse = """
        [
          { "id": $ANIME_ID, "image": { "original": "$ORIGINAL_IMAGE_PATH" } },
          { "id": $OTHER_ANIME_ID, "image": { "original": "$ORIGINAL_IMAGE_PATH" } }
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

    private fun TestScope.createSource(
        restAnswer: suspend () -> String,
        graphQlStatus: HttpStatusCode = HttpStatusCode.OK,
        graphQlAnswer: suspend () -> String = { NO_POSTERS_JSON },
        dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(testScheduler)
    ): AnimeListSource {
        val engine = MockEngine(
            MockEngineConfig().apply {
                this.dispatcher = dispatcher
                addHandler { request ->
                    val isGraphQl = request.url.encodedPath.endsWith("/$GRAPHQL_APPEND_URL")
                    if (isGraphQl) graphQlRequests += request else restRequests += request
                    respond(
                        content = ByteReadChannel(
                            if (isGraphQl) graphQlAnswer() else restAnswer()
                        ),
                        status = if (isGraphQl) graphQlStatus else HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        )
        val service = ShikimoriApiServiceImpl(createHttpClient(engine))
        val safeApi = SafeApiFake()
        return AnimeListSourceImpl(
            service = service,
            safeApi = safeApi,
            posterSource = AnimePosterSourceImpl(service = service, safeApi = safeApi)
        )
    }

    private fun TestScope.createFailingSource(): AnimeListSource =
        createSource(restAnswer = { throw IllegalStateException(TRANSPORT_FAILURE_MESSAGE) })

    @Test
    fun theOngoingListIsAskedForByItsReleaseStatusAndComesBackParsed() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListJsonResponse })

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
        assertEquals("ongoing", restRequests.single().url.parameters["status"])
        assertEquals("popularity", restRequests.single().url.parameters["order"])
        assertEquals(PAGE.toString(), restRequests.single().url.parameters["page"])
        assertNull(restRequests.single().url.parameters["search"])
    }

    @Test
    fun theAnnouncedListIsAskedForByItsOwnReleaseStatus() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListJsonResponse })

        //When
        val result = source.getAnnouncedList(page = PAGE, sort = SortData.SCORE)

        //Then
        assertEquals("anons", restRequests.single().url.parameters["status"])
        assertEquals("ranked", restRequests.single().url.parameters["order"])
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun aSearchIsSentAsAQueryWithNoReleaseStatus() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListJsonResponse })

        //When
        val result = source.getListBySearch(
            page = PAGE,
            search = "frieren",
            sort = SortData.POPULARITY
        )

        //Then
        assertEquals("frieren", restRequests.single().url.parameters["search"])
        assertNull(restRequests.single().url.parameters["status"])
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun anAnimeTheServerSendsWithoutAnIdIsLeftOutOfTheList() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListWithAnEntryMissingItsId })

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(ANIME_ID), result.value.map(ListItemDomain::id))
    }

    @Test
    fun oneAnimeIsAskedForByIdAndComesBackParsed() = runTest {
        //Given
        val source = createSource(restAnswer = { animeDetailsJsonResponse })

        //When
        val result = source.getItemById(ANIME_ID)

        //Then
        assertEquals(
            CallResult.Success(
                expectedListItem.copy(nextEpisodeAt = "2026-01-05T18:00:00.000+03:00")
            ),
            result
        )
        assertTrue(restRequests.single().url.encodedPath.endsWith("/api/animes/$ANIME_ID"))
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

    @Test
    fun aPosterReplacesTheRestImageWhileAnAnimeWithoutOneKeepsIt() = runTest {
        //Given
        val source = createSource(
            restAnswer = { twoAnimeJsonResponse },
            graphQlAnswer = { postersJson(ANIME_ID to POSTER_URL) }
        )

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.Success)
        assertEquals(
            listOf(POSTER_URL, SHIKIMORI_BASE_URL + ORIGINAL_IMAGE_PATH),
            result.value.map(ListItemDomain::imageUrl)
        )
    }

    @Test
    fun onlyThePageIdsAreLookedUpWithTheirCountAsTheLimit() = runTest {
        //Given
        val source = createSource(
            restAnswer = {
                """[{ "name": "No id at all" }, { "id": $ANIME_ID }, """ +
                    """{ "id": $OTHER_ANIME_ID }, { "id": $THIRD_ANIME_ID }]"""
            }
        )

        //When
        source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        val variables = graphQlVariables(graphQlRequests.single())
        assertEquals(
            "$ANIME_ID,$OTHER_ANIME_ID,$THIRD_ANIME_ID",
            variables.getValue("ids").jsonPrimitive.content
        )
        // Three, not the server's default of two.
        assertEquals(3, variables.getValue("limit").jsonPrimitive.int)
    }

    @Test
    fun aRepeatedIdStillGetsItsPosterOnBothItems() = runTest {
        //Given
        val source = createSource(
            restAnswer = { "[$frierenJson, $frierenJson]" },
            graphQlAnswer = { postersJson(ANIME_ID to POSTER_URL) }
        )

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(POSTER_URL, POSTER_URL), result.value.map(ListItemDomain::imageUrl))
    }

    @Test
    fun theAnnouncedListAndTheSearchLookUpPostersToo() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListJsonResponse })

        //When
        source.getAnnouncedList(page = PAGE, sort = SortData.SCORE)
        source.getListBySearch(page = PAGE, search = "frieren", sort = SortData.POPULARITY)

        //Then
        assertEquals(2, graphQlRequests.size)
    }

    @Test
    fun aFailedPosterLookupStillShowsThePageWithRestImages() = runTest {
        //Given
        val source = createSource(
            restAnswer = { animeListJsonResponse },
            graphQlStatus = HttpStatusCode.InternalServerError
        )

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun aFailedPageAsksForNoPosters() = runTest {
        //Given
        val source = createFailingSource()

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.Failure)
        assertEquals(listOf(), graphQlRequests)
    }

    @Test
    fun anEmptyPageAsksForNoPosters() = runTest {
        //Given
        val source = createSource(restAnswer = { "[]" })

        //When
        source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertEquals(listOf(), graphQlRequests)
    }

    @Test
    fun oneAnimeDetailsCallAsksForNoPosters() = runTest {
        //Given
        val source = createSource(restAnswer = { animeDetailsJsonResponse })

        //When
        source.getItemById(ANIME_ID)

        //Then
        assertEquals(1, restRequests.size)
        assertEquals(listOf(), graphQlRequests)
    }

    @Test
    fun cancellingThePageWhileItsPostersAreLookedUpDeliversNothing() = runTest {
        //Given
        val source = createSource(
            restAnswer = { animeListJsonResponse },
            graphQlAnswer = { awaitCancellation() },
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        var result: CallResult<List<ListItemDomain>>? = null
        val page = launch {
            result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)
        }
        // No clock advance: past the per-attempt timeout the lookup would fall back instead.
        runCurrent()

        //When
        page.cancelAndJoin()

        //Then
        assertEquals(1, graphQlRequests.size)
        assertTrue(page.isCancelled)
        assertNull(result)
    }

    private fun postersJson(vararg posters: Pair<Int, String>) = posters.joinToString(
        prefix = """{"data":{"animes":[""",
        postfix = "]}}"
    ) { (id, url) -> """{"id":"$id","poster":{"originalUrl":"$url"}}""" }

    private fun graphQlVariables(request: HttpRequestData) =
        Json.parseToJsonElement((request.body as TextContent).text)
            .jsonObject.getValue("variables").jsonObject
}
