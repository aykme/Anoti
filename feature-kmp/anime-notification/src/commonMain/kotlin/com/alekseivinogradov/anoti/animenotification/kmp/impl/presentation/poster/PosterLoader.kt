package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster

import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.decode.BlackholeDecoder
import coil3.disk.DiskCache
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.withTimeoutOrNull
import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Loads an anime poster for a notification through the app-wide Coil loader. Any failure or a
 * stalled host yields no poster, since the notification is still worth posting without one.
 */
internal class PosterLoader(private val platformContext: PlatformContext) {

    // The app-wide loader, so a poster already shown on screen comes from its cache.
    private val imageLoader: ImageLoader by lazy { SingletonImageLoader.get(platformContext) }

    /** Loads the poster decoded into memory. */
    suspend fun loadImage(imageUrl: String?): Image? = execute(imageUrl) { this }?.image

    /**
     * Downloads the poster without decoding it and copies its file into the temporary directory.
     * The copy is named by [posterFileName].
     */
    @OptIn(ExperimentalCoilApi::class)
    suspend fun loadFile(imageUrl: String?): Path? = imageUrl?.let { url: String ->
        execute(url) {
            // The empty image this decoder returns must not reach the cache the screens read.
            decoderFactory(BlackholeDecoder.Factory()).memoryCachePolicy(CachePolicy.DISABLED)
        }?.let { result: SuccessResult ->
            copyFromDiskCache(url = url, diskCacheKey = result.diskCacheKey)
        }
    }

    private fun copyFromDiskCache(url: String, diskCacheKey: String?): Path? {
        val diskCache = imageLoader.diskCache
        if (diskCacheKey == null || diskCache == null) {
            println("$TAG Poster was not cached on disk: $url")
            return null
        }
        return try {
            diskCache.openSnapshot(diskCacheKey)?.use { snapshot: DiskCache.Snapshot ->
                val posterFile = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / posterFileName(url)
                // The cache's file system need not be the one the temporary directory lives on.
                diskCache.fileSystem.read(snapshot.data) {
                    val cachedPoster = this
                    FileSystem.SYSTEM.write(posterFile) { writeAll(cachedPoster) }
                }
                posterFile
            }
        } catch (e: IOException) {
            println("$TAG Poster could not be copied: $e")
            null
        }
    }

    private suspend fun execute(
        imageUrl: String?,
        configure: ImageRequest.Builder.() -> ImageRequest.Builder
    ): SuccessResult? {
        val result = loadWithTimeout(imageUrl, POSTER_TIMEOUT_MILLIS) { url: String ->
            imageLoader.execute(ImageRequest.Builder(platformContext).data(url).configure().build())
        }
        return when (result) {
            is SuccessResult -> result
            // Coil reports a failed load by returning ErrorResult rather than throwing.
            is ErrorResult -> null.also { println("$TAG ${result.throwable}") }
            null -> null
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
    load: suspend (String) -> T
): T? {
    if (imageUrl == null) return null
    return try {
        // The loader has no timeout of its own, and a bounded background job must not stall on it.
        withTimeoutOrNull(timeoutMillis.milliseconds) { load(imageUrl) }
            ?: null.also { println("$TAG Poster load timed out: $imageUrl") }
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

/** The local file name for [imageUrl]'s poster: its last path segment, extension included. */
internal fun posterFileName(imageUrl: String): String =
    POSTER_FILE_PREFIX + imageUrl.substringBefore('?').substringAfterLast('/')

private const val TAG = "ANIME_NOTIFICATION_POSTER"
private const val POSTER_TIMEOUT_MILLIS = 10_000L
private const val POSTER_FILE_PREFIX = "anime_notification_poster_"
