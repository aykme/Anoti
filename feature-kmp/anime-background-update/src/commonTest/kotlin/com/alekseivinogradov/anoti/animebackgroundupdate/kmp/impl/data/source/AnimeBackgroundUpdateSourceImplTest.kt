package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FRIEREN_ID = 61316
private const val BLEACH_ID = 41467
private const val REQUESTED_IDS = "$FRIEREN_ID,$BLEACH_ID"
private const val TRANSPORT_FAILURE_MESSAGE = "no route to host"
private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/61316/a.jpeg"
private const val NEXT_EPISODE_AT = "2026-10-18T17:15:00+03:00"

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeBackgroundUpdateSourceImplTest {

    private val requests = mutableListOf<HttpRequestData>()

    private val frierenJson = """
        {
          "id": "$FRIEREN_ID",
          "name": "Frieren",
          "poster": { "originalUrl": "$POSTER_URL" },
          "episodes": 28,
          "episodesAired": 7,
          "nextEpisodeAt": "$NEXT_EPISODE_AT",
          "airedOn": { "date": "2025-10-01" },
          "releasedOn": { "date": null },
          "score": 9.11,
          "status": "ongoing"
        }
    """.trimIndent()

    private val bleachJson = """{ "id": "$BLEACH_ID", "name": "Bleach", "status": "released" }"""

    private val expectedFrieren = ListItemDomain(
        id = FRIEREN_ID,
        name = "Frieren",
        imageUrl = POSTER_URL,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = NEXT_EPISODE_AT,
        airedOn = "2025-10-01",
        releasedOn = null,
        score = 9.11F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    @Test
    fun theSavedIdsAreAskedForAsOneUnfilteredPage() = runTest {
        //Given
        val source = createSource(answer = { animesJson(frierenJson) })

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        val variables = Json.parseToJsonElement((requests.single().body as TextContent).text)
            .jsonObject.getValue("variables").jsonObject
        assertEquals(REQUESTED_IDS, variables.getValue("ids").jsonPrimitive.content)
        // Asking by id is asking for those anime exactly, so there is never a second page.
        assertEquals(FIRST_PAGE, variables.getValue("page").jsonPrimitive.int)
        // Without a limit the server answers two of the asked ids only.
        assertEquals(ITEMS_PER_PAGE, variables.getValue("limit").jsonPrimitive.int)
        // None of the listing filters may narrow an answer by id.
        assertEquals(setOf("page", "limit", "ids"), variables.keys)
        assertEquals(CallResult.Success(listOf(expectedFrieren)), result)
    }

    @Test
    fun theAnswerIsReadWhateverOrderTheServerSendsItIn() = runTest {
        //Given
        val source = createSource(answer = { animesJson(bleachJson, frierenJson) })

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(BLEACH_ID, FRIEREN_ID), result.value.map(ListItemDomain::id))
    }

    @Test
    fun anAnimeTheServerSendsWithoutAUsableIdIsLeftOutOfTheList() = runTest {
        //Given
        val source = createSource(
            answer = {
                animesJson("""{ "name": "No id at all", "status": "ongoing" }""", frierenJson)
            }
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        // Nothing downstream could match an id-less anime to a saved row.
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(FRIEREN_ID), result.value.map(ListItemDomain::id))
    }

    @Test
    fun anAnswerWithErrorsAndNoDataIsAFailureNotAnEmptyPage() = runTest {
        //Given
        val source = createSource(
            answer = { $$"""{"errors":[{"message":"Variable $ids was provided invalid value"}]}""" }
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        // An empty success would read as "none of these anime exist" and skip every saved row.
        assertTrue(result is CallResult.Failure)
        assertTrue(result.throwable is IllegalStateException)
    }

    @Test
    fun anExceptionFromTheCallComesBackAsAFailureRatherThanEscaping() = runTest {
        //Given
        val source = createSource(
            answer = { throw IllegalStateException(TRANSPORT_FAILURE_MESSAGE) }
        )

        //When
        val result = source.getListByIds(REQUESTED_IDS)

        //Then
        // Which kind of failure it is belongs to SafeApi; what matters here is that the source
        // answers with one instead of letting the throwable out.
        assertTrue(result is CallResult.Failure)
        assertEquals(TRANSPORT_FAILURE_MESSAGE, result.throwable.message)
    }

    private fun animesJson(vararg animes: String) =
        animes.joinToString(prefix = """{"data":{"animes":[""", postfix = "]}}")

    /**
     * The engine answers on the test's dispatcher. Left to itself it picks `Dispatchers.IO`,
     * which takes the answer off the test's virtual clock and onto a second thread.
     */
    private fun TestScope.createSource(answer: suspend () -> String): AnimeBackgroundUpdateSource {
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = UnconfinedTestDispatcher(testScheduler)
                addHandler { request ->
                    requests += request
                    respond(
                        content = ByteReadChannel(answer()),
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
