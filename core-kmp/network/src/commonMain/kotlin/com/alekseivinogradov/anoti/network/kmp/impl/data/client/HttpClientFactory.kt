package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds the [HttpClient] every API service in the app should use, configured with:
 * - `expectSuccess = true`, so any non-2xx response throws an
 *   [io.ktor.client.plugins.ResponseException] instead of returning silently — this is what lets
 *   [com.alekseivinogradov.anoti.network.kmp.impl.data.SafeApiImpl] classify HTTP failures.
 * - JSON content negotiation via kotlinx.serialization, with `ignoreUnknownKeys` so adding new
 *   fields to a backend response doesn't break deserialization.
 *
 * [engine] is the caller's choice of transport (e.g. OkHttp on Android, Darwin on iOS) — this
 * function does not select one itself.
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
}
