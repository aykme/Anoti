package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import com.alekseivinogradov.anoti.network.kmp.api.domain.PER_ATTEMPT_TIMEOUT_MILLIS
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates an [HttpClient] with `expectSuccess = true`, JSON content negotiation, and a bounded
 * per-attempt timeout.
 *
 * @param engine transport to use (e.g. OkHttp, Darwin) — this function doesn't pick one itself.
 */
fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = PER_ATTEMPT_TIMEOUT_MILLIS
        connectTimeoutMillis = PER_ATTEMPT_TIMEOUT_MILLIS
        socketTimeoutMillis = PER_ATTEMPT_TIMEOUT_MILLIS
    }
}
