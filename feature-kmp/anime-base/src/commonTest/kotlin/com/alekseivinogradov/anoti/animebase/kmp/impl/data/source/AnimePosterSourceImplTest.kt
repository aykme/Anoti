package com.alekseivinogradov.anoti.animebase.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.source.AnimePosterSource
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
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/1/a.jpeg"
private const val POSTERS_JSON =
    """{"data":{"animes":[{"id":"1","poster":{"originalUrl":"$POSTER_URL"}}]}}"""

@OptIn(ExperimentalCoroutinesApi::class)
class AnimePosterSourceImplTest {

    private val requests = mutableListOf<HttpRequestData>()

    @Test
    fun theFoundPostersComeBackKeyedById() = runTest {
        //Given
        val source = createSource(UnconfinedTestDispatcher(testScheduler)) { POSTERS_JSON }

        //When
        val posterUrls = source.getPosterUrls(listOf(1, 2))

        //Then
        assertEquals(mapOf(1 to POSTER_URL), posterUrls)
    }

    @Test
    fun noIdsAskTheServerNothing() = runTest {
        //Given
        val source = createSource(UnconfinedTestDispatcher(testScheduler)) { POSTERS_JSON }

        //When
        val posterUrls = source.getPosterUrls(listOf())

        //Then
        assertEquals(mapOf(), posterUrls)
        assertEquals(listOf(), requests)
    }

    @Test
    fun aFailedLookupGivesNoPostersInsteadOfAFailure() = runTest {
        //Given
        val source = createSource(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            status = HttpStatusCode.InternalServerError
        ) { "{}" }

        //When
        val posterUrls = source.getPosterUrls(listOf(1))

        //Then
        // The REST image stays; a missing poster is never an error the user sees.
        assertEquals(mapOf(), posterUrls)
    }

    @Test
    fun cancellingTheLookupCancelsItInsteadOfGivingNoPosters() = runTest {
        //Given
        val source = createSource(StandardTestDispatcher(testScheduler)) { awaitCancellation() }
        var posterUrls: Map<Int, String>? = null
        val lookup = launch { posterUrls = source.getPosterUrls(listOf(1)) }
        // No clock advance: past the per-attempt timeout the lookup would fail, not hang.
        runCurrent()

        //When
        lookup.cancelAndJoin()

        //Then
        assertEquals(1, requests.size)
        assertTrue(lookup.isCancelled)
        assertNull(posterUrls)
    }

    @Test
    fun eachKindOfFailureIsNamedWithoutItsMessage() {
        //Given
        val failures = listOf(
            CallResult.HttpError(code = 500, throwable = IllegalStateException("secret")),
            CallResult.NetworkError(IOException("secret")),
            CallResult.OtherError(IllegalStateException("secret"))
        )

        //When
        val descriptions = failures.map(::describeFailure)

        //Then
        assertEquals(listOf("HTTP 500", "network error", "other error"), descriptions)
    }

    private fun createSource(
        dispatcher: CoroutineDispatcher,
        status: HttpStatusCode = HttpStatusCode.OK,
        answer: suspend () -> String
    ): AnimePosterSource {
        val engine = MockEngine(
            MockEngineConfig().apply {
                this.dispatcher = dispatcher
                addHandler { request ->
                    requests += request
                    respond(
                        content = ByteReadChannel(answer()),
                        status = status,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        )
        return AnimePosterSourceImpl(
            service = ShikimoriApiServiceImpl(createHttpClient(engine)),
            safeApi = SafeApiFake()
        )
    }
}
