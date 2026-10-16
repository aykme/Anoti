package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val ANIME_ID = 61316
private const val PAGE = 2
private const val TRANSPORT_FAILURE_MESSAGE = "no route to host"
private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/61316/a.jpeg"
private const val NEXT_EPISODE_AT = "2026-01-05T18:00:00+03:00"

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeListSourceImplTest {

    private val requests = mutableListOf<HttpRequestData>()

    private val frierenJson = """
        {
          "id": "$ANIME_ID",
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

    private val animeListJsonResponse = animesJson(frierenJson)

    private val expectedListItem = ListItemDomain(
        id = ANIME_ID,
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

    private fun TestScope.createSource(answer: suspend () -> String): AnimeListSource {
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
        return AnimeListSourceImpl(
            service = ShikimoriApiServiceImpl(createHttpClient(engine)),
            safeApi = SafeApiFake()
        )
    }

    private fun TestScope.createFailingSource(): AnimeListSource =
        createSource(answer = { throw IllegalStateException(TRANSPORT_FAILURE_MESSAGE) })

    @Test
    fun theOngoingListIsAskedForByItsReleaseStatusAndComesBackParsed() = runTest {
        //Given
        val source = createSource(answer = { animeListJsonResponse })

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
        val variables = variables(requests.single())
        assertEquals("ongoing", variables.getValue("status").jsonPrimitive.content)
        assertEquals("popularity", variables.getValue("order").jsonPrimitive.content)
        assertEquals(PAGE.toString(), variables.getValue("page").jsonPrimitive.content)
        assertFalse("search" in variables)
    }

    @Test
    fun theAnnouncedListIsAskedForByItsOwnReleaseStatus() = runTest {
        //Given
        val source = createSource(answer = { animeListJsonResponse })

        //When
        val result = source.getAnnouncedList(page = PAGE, sort = SortData.SCORE)

        //Then
        val variables = variables(requests.single())
        assertEquals("anons", variables.getValue("status").jsonPrimitive.content)
        assertEquals("ranked", variables.getValue("order").jsonPrimitive.content)
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun aSearchIsSentAsAQueryWithNoReleaseStatus() = runTest {
        //Given
        val source = createSource(answer = { animeListJsonResponse })

        //When
        val result = source.getListBySearch(
            page = PAGE,
            search = "frieren",
            sort = SortData.POPULARITY
        )

        //Then
        val variables = variables(requests.single())
        assertEquals("frieren", variables.getValue("search").jsonPrimitive.content)
        assertFalse("status" in variables)
        assertEquals(CallResult.Success(listOf(expectedListItem)), result)
    }

    @Test
    fun anAnimeTheServerSendsWithoutAUsableIdIsLeftOutOfTheList() = runTest {
        //Given
        val source = createSource(
            answer = {
                animesJson(
                    """{ "name": "No id at all", "status": "ongoing" }""",
                    """{ "id": "z1", "name": "Not a number", "status": "ongoing" }""",
                    frierenJson
                )
            }
        )

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.Success)
        assertEquals(listOf(ANIME_ID), result.value.map(ListItemDomain::id))
    }

    @Test
    fun anAnswerWithErrorsAndNoDataFailsThePage() = runTest {
        //Given
        val source = createSource(
            answer = { """{"errors":[{"message":"Variable ${'$'}order was provided invalid value"}]}""" }
        )

        //When
        val result = source.getOngoingList(page = PAGE, sort = SortData.POPULARITY)

        //Then
        assertTrue(result is CallResult.OtherError)
        assertTrue(result.throwable is IllegalStateException)
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

    private fun animesJson(vararg animes: String) =
        animes.joinToString(prefix = """{"data":{"animes":[""", postfix = "]}}")

    private fun variables(request: HttpRequestData): JsonObject =
        Json.parseToJsonElement((request.body as TextContent).text)
            .jsonObject.getValue("variables").jsonObject
}
