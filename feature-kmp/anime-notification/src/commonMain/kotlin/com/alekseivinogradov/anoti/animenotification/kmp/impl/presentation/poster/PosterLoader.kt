package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster

import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Loads an anime poster for a notification through the app-wide Coil loader. Any failure or a
 * stalled host yields no poster, since the notification is still worth posting without one.
 */
class PosterLoader(private val platformContext: PlatformContext) {

    // The app-wide loader, so a poster already shown on screen comes from its cache.
    private val imageLoader: ImageLoader by lazy { SingletonImageLoader.get(platformContext) }

    suspend fun load(imageUrl: String?): Image? =
        loadWithTimeout(imageUrl = imageUrl, timeoutMillis = POSTER_TIMEOUT_MILLIS) { url: String ->
            val request = ImageRequest.Builder(platformContext).data(url).build()
            when (val result = imageLoader.execute(request)) {
                is SuccessResult -> result.image
                // Coil reports a failed load by returning ErrorResult rather than throwing.
                is ErrorResult -> null.also { println("$TAG ${result.throwable}") }
            }
        }
}

/**
 * Runs [load] for [imageUrl] within [timeoutMillis]. A missing URL, a timeout or a thrown
 * exception all yield `null`. Cancellation still propagates.
 */
internal suspend fun <T : Any> loadWithTimeout(
    imageUrl: String?,
    timeoutMillis: Long,
    load: suspend (String) -> T?
): T? {
    if (imageUrl == null) return null
    return try {
        // The loader has no timeout of its own, and a bounded background job must not stall on it.
        val outcome = withTimeoutOrNull(timeoutMillis.milliseconds) {
            LoadOutcome(load(imageUrl))
        }
        if (outcome == null) println("$TAG Poster load timed out: $imageUrl")
        outcome?.poster
    } catch (e: CancellationException) {
        throw e
    } catch (
        // Best-effort poster load; falling back to no image on any other failure is the point.
        @Suppress("TooGenericExceptionCaught") e: Exception
    ) {
        println("$TAG $e")
        null
    }
}

// Tells a finished load that found no poster apart from a timeout.
private class LoadOutcome<T : Any>(val poster: T?)

private const val TAG = "ANIME_NOTIFICATION_POSTER"
private const val POSTER_TIMEOUT_MILLIS = 10_000L
