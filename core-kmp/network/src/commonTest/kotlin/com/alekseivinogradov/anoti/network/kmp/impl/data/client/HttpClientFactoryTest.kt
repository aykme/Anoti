package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class HttpClientFactoryTest {

    @Serializable
    private data class TestPayload(val value: String)

    @Test
    fun decodesJsonResponseBodyIntoSerializableModel() = runTest {
        //Given
        val client = createHttpClient(
            MockEngine {
                respond(
                    content = ByteReadChannel("""{"value":"hello"}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

        //When
        val result: TestPayload = client.get("https://test/").body()

        //Then
        assertEquals(TestPayload("hello"), result)
    }

    @Test
    fun throwsResponseExceptionOnNonSuccessStatus() = runTest {
        //Given
        val client = createHttpClient(
            MockEngine {
                respond(content = ByteReadChannel(""), status = HttpStatusCode.NotFound)
            }
        )

        //When / Then
        assertFailsWith<ResponseException> {
            client.get("https://test/")
        }
    }
}
