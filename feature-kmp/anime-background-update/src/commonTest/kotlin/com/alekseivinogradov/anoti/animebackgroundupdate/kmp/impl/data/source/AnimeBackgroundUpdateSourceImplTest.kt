package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
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
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeBackgroundUpdateSourceImplTest {

    private lateinit var lastRequest: HttpRequestData

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
        val source = createSource(animeListJsonResponse, UnconfinedTestDispatcher(testScheduler))

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        assertEquals(REQUESTED_IDS, lastRequest.url.parameters["ids"])
        // Asking by id is asking for those anime exactly, so there is never a second page.
        assertEquals("1", lastRequest.url.parameters["page"])
        // Asking by id means asking for those anime whatever they are, so none of the listing
        // filters may narrow the answer.
        assertNull(lastRequest.url.parameters["status"])
        assertNull(lastRequest.url.parameters["order"])
        assertNull(lastRequest.url.parameters["search"])
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun anAnimeTheServerSendsWithoutAnIdIsLeftOutOfTheList() = runTest {
        //Given
        val source = createSource(
            animeListWithAnEntryMissingItsId,
            UnconfinedTestDispatcher(testScheduler)
        )

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
        val source = AnimeBackgroundUpdateSourceImpl(
            service = ShikimoriApiServiceImpl(
                createHttpClient(
                    MockEngine(
                        MockEngineConfig().apply {
                            dispatcher = UnconfinedTestDispatcher(testScheduler)
                            addHandler { throw IllegalStateException(TRANSPORT_FAILURE_MESSAGE) }
                        }
                    )
                )
            ),
            safeApi = SafeApiFake()
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        // Which kind of failure it is belongs to SafeApi; what matters here is that the source
        // answers with one instead of letting the throwable out.
        assertTrue(result is CallResult.Failure)
        assertEquals(TRANSPORT_FAILURE_MESSAGE, result.throwable.message)
    }

    /**
     * @param dispatcher what the engine answers on. Left to itself it picks `Dispatchers.IO`,
     * which takes the answer off the test's virtual clock and onto a second thread.
     */
    private fun createSource(
        jsonResponse: String,
        dispatcher: CoroutineDispatcher
    ): AnimeBackgroundUpdateSource {
        val engine = MockEngine(
            MockEngineConfig().apply {
                this.dispatcher = dispatcher
                addHandler { request ->
                    lastRequest = request
                    respond(
                        content = ByteReadChannel(jsonResponse),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        )
        return AnimeBackgroundUpdateSourceImpl(
            service = ShikimoriApiServiceImpl(createHttpClient(engine)),
            safeApi = SafeApiFake()
        )
    }
}
