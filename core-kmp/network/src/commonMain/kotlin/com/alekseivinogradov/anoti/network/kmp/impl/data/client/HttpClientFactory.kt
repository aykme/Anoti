package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import com.alekseivinogradov.anoti.network.kmp.api.domain.PER_ATTEMPT_TIMEOUT_MILLIS
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_USER_AGENT
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates an [HttpClient] with `expectSuccess = true`, JSON content negotiation, a bounded
 * per-attempt timeout, and the app's name as its `User-Agent`.
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
    nameTheApp()
}

/**
 * Creates the [HttpClient] images load through, with the app's name as its `User-Agent` and the
 * engine's defaults otherwise, timeouts included. The image loader checks statuses and caches on
 * its own.
 *
 * @param engine transport to use (e.g. OkHttp, Darwin) — this function doesn't pick one itself.
 */
fun createImageHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    nameTheApp()
}

private fun HttpClientConfig<*>.nameTheApp() {
    install(UserAgent) {
        agent = SHIKIMORI_USER_AGENT
    }
}
