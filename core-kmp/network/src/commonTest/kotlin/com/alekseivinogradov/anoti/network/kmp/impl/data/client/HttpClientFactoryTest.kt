package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_USER_AGENT
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class HttpClientFactoryTest {

    @Serializable
    private data class TestPayload(val value: String)

    @Test
    fun decodesJsonResponseBodyIntoSerializableModel() = runTest {
        //Given
        val client = createTestClient {
            respond(
                content = ByteReadChannel("""{"value":"hello"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        //When
        val result: TestPayload = client.get("https://test/").body()

        //Then
        assertEquals(TestPayload("hello"), result)
    }

    @Test
    fun throwsResponseExceptionOnNonSuccessStatus() = runTest {
        //Given
        val client = createTestClient {
            respond(content = ByteReadChannel(""), status = HttpStatusCode.NotFound)
        }

        //When / Then
        assertFailsWith<ResponseException> {
            client.get("https://test/")
        }
    }

    @Test
    fun namesTheAppInTheUserAgentOfEveryRequest() = runTest {
        //Given
        var userAgent: String? = null
        val client = createTestClient { request ->
            userAgent = request.headers[HttpHeaders.UserAgent]
            respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
        }

        //When
        client.get("https://test/")

        //Then
        assertEquals(SHIKIMORI_USER_AGENT, userAgent)
    }

    /** The app's client over [handler], answering on the test's virtual clock. */
    private fun TestScope.createTestClient(handler: MockRequestHandler) = createHttpClient(
        MockEngine(
            MockEngineConfig().apply {
                dispatcher = UnconfinedTestDispatcher(testScheduler)
                addHandler(handler)
            }
        )
    )
}
