package com.alekseivinogradov.anoti.animebase.kmp.impl.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListDataResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.IncompleteDateResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.PosterResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val POSTER_URL =
    "https://shikimori.io/uploads/poster/animes/61469/b114b577e574ab981ce0cc8cfe4cec99.jpeg"

@OptIn(ExperimentalCoroutinesApi::class)
class ShikimoriApiServiceImplTest {

    private lateinit var lastRequest: HttpRequestData

    private val animeListJsonResponse = """
        {
          "data": {
            "animes": [
              {
                "id": "61469",
                "name": "Steel Ball Run",
                "poster": { "originalUrl": "$POSTER_URL" },
                "episodes": 24,
                "episodesAired": 3,
                "nextEpisodeAt": "2026-10-18T17:15:00+03:00",
                "airedOn": { "date": "2026-09-27" },
                "releasedOn": { "date": null },
                "score": 8.9,
                "status": "ongoing"
              }
            ]
          }
        }
    """.trimIndent()

    private val emptyAnimeListJsonResponse = """{"data":{"animes":[]}}"""

    @Test
    fun aListPagePostsItsVariablesToGraphQlAndDecodesTheAnswer() = runTest {
        //Given
        val service = createService(animeListJsonResponse)

        //When
        val result = service.getAnimeList(
            page = 2,
            releaseStatus = "ongoing",
            sort = "ranked",
            search = "\"steel\" & 100%",
            ids = null
        )

        //Then
        assertEquals(
            AnimeListResponse(
                data = AnimeListDataResponse(
                    animes = listOf(
                        AnimeResponse(
                            id = "61469",
                            englishName = "Steel Ball Run",
                            poster = PosterResponse(POSTER_URL),
                            episodesTotal = 24,
                            episodesAired = 3,
                            nextEpisodeAt = "2026-10-18T17:15:00+03:00",
                            airedOn = IncompleteDateResponse(date = "2026-09-27"),
                            releasedOn = IncompleteDateResponse(date = null),
                            score = 8.9F,
                            releaseStatus = "ongoing"
                        )
                    )
                )
            ),
            result
        )
        assertEquals(HttpMethod.Post, lastRequest.method)
        assertTrue(lastRequest.url.encodedPath.endsWith("/api/graphql"))
        val content = lastRequest.body as TextContent
        assertEquals(ContentType.Application.Json, content.contentType.withoutParameters())
        val body = Json.parseToJsonElement(content.text).jsonObject
        val variables = body.getValue("variables").jsonObject
        assertEquals(2, variables.getValue("page").jsonPrimitive.int)
        // Without a limit the server answers two anime only.
        assertEquals(ITEMS_PER_PAGE, variables.getValue("limit").jsonPrimitive.int)
        assertEquals("ongoing", variables.getValue("status").jsonPrimitive.content)
        assertEquals("ranked", variables.getValue("order").jsonPrimitive.content)
        assertEquals("\"steel\" & 100%", variables.getValue("search").jsonPrimitive.content)
        assertFalse("ids" in variables)
        val query = body.getValue("query").jsonPrimitive.content
        assertTrue("nextEpisodeAt" in query)
        assertTrue("poster { originalUrl }" in query)
    }

    @Test
    fun anIdsLookupLeavesOutEveryFilterItHasNoValueFor() = runTest {
        //Given
        val service = createService(emptyAnimeListJsonResponse)

        //When
        service.getAnimeList(page = 1, releaseStatus = null, sort = null, search = null, ids = "1,2")

        //Then
        val variables = Json.parseToJsonElement((lastRequest.body as TextContent).text)
            .jsonObject.getValue("variables").jsonObject
        assertEquals("1,2", variables.getValue("ids").jsonPrimitive.content)
        // Sent as null, an order would change the ranking rather than leave the default.
        assertEquals(setOf("page", "limit", "ids"), variables.keys)
    }

    private fun TestScope.createService(jsonResponse: String): ShikimoriApiService {
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = UnconfinedTestDispatcher(testScheduler)
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
        return ShikimoriApiServiceImpl(createHttpClient(engine))
    }
}
