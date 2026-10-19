package com.alekseivinogradov.anoti.network.kmp.impl.data.client

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient

/**
 * Creates the app's [ImageLoader]: Coil's defaults, with remote images fetched through
 * [httpClient].
 *
 * @param context context the loader is built against.
 * @param httpClient supplies the client on the first fetch, so building the loader stays cheap.
 */
fun createImageLoader(context: PlatformContext, httpClient: () -> HttpClient): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(KtorNetworkFetcherFactory(httpClient = httpClient)) }
        .build()
