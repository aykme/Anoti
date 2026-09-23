package com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ANIME_ID = 61316
private const val ORIGINAL_IMAGE_PATH = "/system/animes/original/61316.jpg"

class AnimeFavoritesSourceImplTest {

    private val animeDetailsJsonResponse = """
        {
          "id": $ANIME_ID,
          "name": "Frieren",
          "russian": "Фрирен",
          "url": "/animes/$ANIME_ID",
          "image": {
            "original": "$ORIGINAL_IMAGE_PATH",
            "preview": "/system/animes/preview/61316.jpg"
          },
          "episodes_aired": 7,
          "episodes": 28,
          "next_episode_at": "2026-01-05T18:00:00.000+03:00",
          "aired_on": "2025-10-01",
          "released_on": null,
          "score": 9.11,
          "status": "ongoing",
          "kind": "tv",
          "description": "desc"
        }
    """.trimIndent()

    private fun createSource(engine: MockEngine): AnimeFavoritesSource = AnimeFavoritesSourceImpl(
        service = ShikimoriApiServiceImpl(createHttpClient(engine)),
        safeApi = SafeApiFake()
    )

    @Test
    fun anAnimeTheServerAnswersWithComesBackAsAFavoritesItem() = runTest {
        //Given
        val source = createSource(
            MockEngine {
                respond(
                    content = ByteReadChannel(animeDetailsJsonResponse),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

        //When
        val result: CallResult<ListItemDomain> = source.getItemById(ANIME_ID)

        //Then
        assertEquals(
            CallResult.Success(
                ListItemDomain(
                    id = ANIME_ID,
                    name = "Frieren",
                    imageUrl = SHIKIMORI_BASE_URL + ORIGINAL_IMAGE_PATH,
                    episodesAired = 7,
                    episodesTotal = 28,
                    nextEpisodeAt = "2026-01-05T18:00:00.000+03:00",
                    airedOn = "2025-10-01",
                    releasedOn = null,
                    score = 9.11F,
                    releaseStatus = ReleaseStatusDomain.ONGOING,
                    episodesViewed = 0,
                    isNewEpisode = false
                )
            ),
            result
        )
    }

    @Test
    fun aCallThatNeverReachesTheServerComesBackAsAnError() = runTest {
        //Given
        val failureMessage = "no route to host"
        val source = createSource(MockEngine { throw IllegalStateException(failureMessage) })

        //When
        val result: CallResult<ListItemDomain> = source.getItemById(ANIME_ID)

        //Then
        assertTrue(result is CallResult.OtherError)
        assertEquals(failureMessage, result.throwable.message)
    }
}
