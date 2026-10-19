package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import coil3.annotation.ExperimentalCoilApi
import coil3.decode.BlackholeDecoder
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_USER_AGENT
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

private const val POSTER_URL = "https://shikimori.io/uploads/poster/animes/1/a.jpeg"

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ImageLoaderFactoryTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun aRemoteImageIsFetchedThroughTheGivenClient() = runTest {
        //Given
        val requests = mutableListOf<String?>()
        val loader = createImageLoader(context) {
            createImageHttpClient(
                MockEngine(
                    MockEngineConfig().apply {
                        dispatcher = UnconfinedTestDispatcher(testScheduler)
                        addHandler { request ->
                            requests += request.headers[HttpHeaders.UserAgent]
                            respond(content = ByteReadChannel("poster"), status = HttpStatusCode.OK)
                        }
                    }
                )
            )
        }

        //When
        val result = loader.execute(posterRequest())

        //Then
        assertIs<SuccessResult>(result)
        assertEquals(listOf<String?>(SHIKIMORI_USER_AGENT), requests)
    }

    @Test
    fun theLoaderKeepsADiskCache() {
        //Given
        val loader = createImageLoader(context) { error("no image is fetched here") }

        //When
        val diskCache = loader.diskCache

        //Then
        // A notification's poster is copied out of this cache.
        assertNotNull(diskCache)
    }

    // Nothing is decoded and nothing is kept, so the test sees only the fetch.
    @OptIn(ExperimentalCoilApi::class)
    private fun posterRequest(): ImageRequest = ImageRequest.Builder(context)
        .data(POSTER_URL)
        .decoderFactory(BlackholeDecoder.Factory())
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()
}
