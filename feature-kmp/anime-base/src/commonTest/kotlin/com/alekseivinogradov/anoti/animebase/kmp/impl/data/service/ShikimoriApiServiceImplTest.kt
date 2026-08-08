package com.alekseivinogradov.anoti.animebase.kmp.impl.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.ImageResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

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

    private fun createService(jsonResponse: String): ShikimoriApiService {
        val engine = MockEngine { request ->
            lastRequest = request
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return ShikimoriApiServiceImpl(createHttpClient(engine))
    }
}
