package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.fake

import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.ImageResult

/**
 * Answers every request from [onExecute] instead of fetching anything, so a poster load never
 * leaves the test.
 *
 * @param onExecute the result to answer a request with.
 * @param cache the disk cache to report, or null for a loader that caches nothing.
 */
internal class ImageLoaderFake(
    private val onExecute: (ImageRequest) -> ImageResult,
    private val cache: DiskCache? = null
) : ImageLoader {

    override val defaults: ImageRequest.Defaults = ImageRequest.Defaults.DEFAULT

    override val components: ComponentRegistry = ComponentRegistry()

    override val memoryCache: MemoryCache? = null

    override val diskCache: DiskCache? get() = cache

    override suspend fun execute(request: ImageRequest): ImageResult = onExecute(request)

    override fun enqueue(request: ImageRequest): Disposable = error("not used in tests")

    override fun newBuilder(): ImageLoader.Builder = error("not used in tests")

    override fun shutdown() = Unit
}
