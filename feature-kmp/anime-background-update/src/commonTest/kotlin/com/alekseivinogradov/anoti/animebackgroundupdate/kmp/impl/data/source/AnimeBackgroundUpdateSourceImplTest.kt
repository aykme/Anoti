package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.GRAPHQL_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.source.AnimePosterSourceImpl
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val FRIEREN_ID = 61316
private const val BLEACH_ID = 41467
private const val ORIGINAL_IMAGE_PATH = "/system/animes/original/61316.jpg"
private const val REQUESTED_IDS = "$FRIEREN_ID,$BLEACH_ID"
private const val TRANSPORT_FAILURE_MESSAGE = "no route to host"
private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/61316/a.jpeg"
private const val NO_POSTERS_JSON = """{"data":{"animes":[]}}"""

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeBackgroundUpdateSourceImplTest {

    private val restRequests = mutableListOf<HttpRequestData>()
    private val graphQlRequests = mutableListOf<HttpRequestData>()

    private val animeListJsonResponse = """
        [
          {
            "id": $FRIEREN_ID,
            "name": "Frieren",
            "russian": "Фрирен",
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
          { "id": $FRIEREN_ID, "name": "Frieren", "status": "ongoing" }
        ]
    """.trimIndent()

    private val expectedListItem = ListItemDomain(
        id = FRIEREN_ID,
        name = "Frieren",
        imageUrl = SHIKIMORI_BASE_URL + ORIGINAL_IMAGE_PATH,
        episodesAired = 7,
        episodesTotal = 28,
        airedOn = "2025-10-01",
        releasedOn = null,
        score = 9.11F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    @Test
    fun theSavedIdsAreAskedForAsOneUnfilteredPage() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListJsonResponse })

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        val request = restRequests.single()
        assertEquals(REQUESTED_IDS, request.url.parameters["ids"])
        // Asking by id is asking for those anime exactly, so there is never a second page.
        assertEquals("1", request.url.parameters["page"])
        // Asking by id means asking for those anime whatever they are, so none of the listing
        // filters may narrow the answer.
        assertNull(request.url.parameters["status"])
        assertNull(request.url.parameters["order"])
        assertNull(request.url.parameters["search"])
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun anAnimeTheServerSendsWithoutAnIdIsLeftOutOfTheList() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListWithAnEntryMissingItsId })

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        // Nothing downstream could match an id-less anime to a saved row.
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(FRIEREN_ID), result.value.map(ListItemDomain::id))
    }

    @Test
    fun anExceptionFromTheCallComesBackAsAFailureRatherThanEscaping() = runTest {
        //Given
        val source = createSource(
            restAnswer = { throw IllegalStateException(TRANSPORT_FAILURE_MESSAGE) }
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        // Which kind of failure it is belongs to SafeApi; what matters here is that the source
        // answers with one instead of letting the throwable out.
        assertTrue(result is CallResult.Failure)
        assertEquals(TRANSPORT_FAILURE_MESSAGE, result.throwable.message)
    }

    @Test
    fun aPosterReplacesTheRestImage() = runTest {
        //Given
        val source = createSource(
            restAnswer = { animeListJsonResponse },
            graphQlAnswer = {
                """{"data":{"animes":[""" +
                    """{"id":"$FRIEREN_ID","poster":{"originalUrl":"$POSTER_URL"}}]}}"""
            }
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        assertEquals(
            CallResult.Success(listOf(expectedListItem.copy(imageUrl = POSTER_URL))),
            result
        )
    }

    @Test
    fun aFailedPosterLookupKeepsTheRestImages() = runTest {
        //Given
        val source = createSource(
            restAnswer = { animeListJsonResponse },
            graphQlStatus = HttpStatusCode.InternalServerError
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun thePostersAreLookedUpForTheAnimeTheServerSent() = runTest {
        //Given
        val source = createSource(restAnswer = { animeListJsonResponse })

        //When
        source.getListByIds(REQUESTED_IDS)

        //Then
        // Two ids were asked for; the server sent one, so one is looked up.
        val body = (graphQlRequests.single().body as TextContent).text
        assertTrue(""""ids":"$FRIEREN_ID"""" in body, body)
    }

    /**
     * The engine answers on the test's dispatcher. Left to itself it picks `Dispatchers.IO`,
     * which takes the answer off the test's virtual clock and onto a second thread.
     */
    private fun TestScope.createSource(
        restAnswer: suspend () -> String,
        graphQlStatus: HttpStatusCode = HttpStatusCode.OK,
        graphQlAnswer: suspend () -> String = { NO_POSTERS_JSON }
    ): AnimeBackgroundUpdateSource {
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = UnconfinedTestDispatcher(testScheduler)
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
        return AnimeBackgroundUpdateSourceImpl(
            service = service,
            safeApi = safeApi,
            posterSource = AnimePosterSourceImpl(service = service, safeApi = safeApi)
        )
    }
}
