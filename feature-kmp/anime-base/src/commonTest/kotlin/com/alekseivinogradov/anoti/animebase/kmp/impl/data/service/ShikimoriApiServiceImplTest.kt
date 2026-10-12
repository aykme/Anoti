package com.alekseivinogradov.anoti.animebase.kmp.impl.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePosterResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePostersDataResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePostersResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.ImageResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.PosterResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val POSTER_URL =
    "https://shikimori.io/uploads/poster/animes/61469/b114b577e574ab981ce0cc8cfe4cec99.jpeg"

@OptIn(ExperimentalCoroutinesApi::class)
class ShikimoriApiServiceImplTest {

    private lateinit var lastRequest: HttpRequestData

    private val animeListJsonResponse = """
        [
          {
            "id": 1,
            "name": "Attack on Titan",
            "russian": "Атака титанов",
            "url": "/animes/1",
            "image": {
              "original": "/o.jpg",
              "preview": "/p.jpg",
              "x96": "/x96.jpg",
              "x48": "/x48.jpg"
            },
            "episodes_aired": 12,
            "episodes": 25,
            "aired_on": "2013-04-07",
            "released_on": "2013-09-28",
            "score": 8.53,
            "status": "released",
            "kind": "tv"
          }
        ]
    """.trimIndent()

    private val emptyAnimeListJsonResponse = "[]"

    private val animeDetailsJsonResponse = """
        {
          "id": 5,
          "name": "Bleach",
          "russian": "Блич",
          "url": "/animes/5",
          "image": null,
          "episodes_aired": 10,
          "episodes": null,
          "next_episode_at": "2024-12-28T17:00:00+03:00",
          "aired_on": "2004-10-05",
          "released_on": null,
          "score": 8.97,
          "status": "ongoing",
          "kind": "tv",
          "description": "desc"
        }
    """.trimIndent()

    private val animePostersJsonResponse = """
        {
          "data": {
            "animes": [
              { "id": "61469", "poster": { "originalUrl": "$POSTER_URL" } },
              { "id": "35891", "poster": null }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun getAnimeListSendsQueryParamsAndDecodesResponse() = runTest {
        //Given
        val service = createService(animeListJsonResponse)

        //When
        val result = service.getAnimeList(
            page = 2,
            releaseStatus = "ongoing",
            sort = "popularity",
            search = "titan",
            ids = "1,2"
        )

        //Then
        assertEquals(
            listOf(
                AnimeShortResponse(
                    id = 1,
                    englishName = "Attack on Titan",
                    russianName = "Атака титанов",
                    pageUrl = "/animes/1",
                    imageResponse = ImageResponse(
                        originalSizeUrl = "/o.jpg",
                        previewSizeUrl = "/p.jpg",
                        x96SizeUrl = "/x96.jpg",
                        x48SizeUrl = "/x48.jpg"
                    ),
                    episodesAired = 12,
                    episodesTotal = 25,
                    airedOn = "2013-04-07",
                    releasedOn = "2013-09-28",
                    score = 8.53F,
                    releaseStatus = "released",
                    kind = "tv"
                )
            ),
            result
        )
        val params = lastRequest.url.parameters
        assertEquals("2", params["page"])
        assertEquals("ongoing", params["status"])
        assertEquals("popularity", params["order"])
        assertEquals("titan", params["search"])
        assertEquals("1,2", params["ids"])
    }

    @Test
    fun getAnimeListOmitsNullOptionalParams() = runTest {
        //Given
        val service = createService(emptyAnimeListJsonResponse)

        //When
        service.getAnimeList(page = 1, releaseStatus = null, sort = null, search = null, ids = null)

        //Then
        val params = lastRequest.url.parameters
        assertNull(params["status"])
        assertNull(params["order"])
        assertNull(params["search"])
        assertNull(params["ids"])
    }

    @Test
    fun getAnimeByIdRequestsExpectedPathAndDecodesResponse() = runTest {
        //Given
        val service = createService(animeDetailsJsonResponse)

        //When
        val result = service.getAnimeById(5)

        //Then
        assertEquals(
            AnimeDetailsResponse(
                id = 5,
                englishName = "Bleach",
                russianName = "Блич",
                pageUrl = "/animes/5",
                imageResponse = null,
                episodesAired = 10,
                episodesTotal = null,
                nextEpisodeAt = "2024-12-28T17:00:00+03:00",
                airedOn = "2004-10-05",
                releasedOn = null,
                score = 8.97F,
                releaseStatus = "ongoing",
                kind = "tv",
                description = "desc"
            ),
            result
        )
        assertTrue(lastRequest.url.encodedPath.endsWith("/api/animes/5"))
    }

    @Test
    fun getAnimePostersPostsTheIdsAndTheirCountToGraphQlAndDecodesTheAnswer() = runTest {
        //Given
        val service = createService(animePostersJsonResponse)

        //When
        val result = service.getAnimePosters(listOf(61469, 35891))

        //Then
        assertEquals(
            AnimePostersResponse(
                data = AnimePostersDataResponse(
                    animes = listOf(
                        AnimePosterResponse(id = "61469", poster = PosterResponse(POSTER_URL)),
                        AnimePosterResponse(id = "35891", poster = null)
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
        assertEquals("61469,35891", variables.getValue("ids").jsonPrimitive.content)
        // Without a limit the server answers two anime only.
        assertEquals(2, variables.getValue("limit").jsonPrimitive.int)
        assertTrue("poster { originalUrl }" in body.getValue("query").jsonPrimitive.content)
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
