package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster

import android.graphics.Bitmap
import coil3.Image
import coil3.asImage
import coil3.disk.DiskCache
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.fake.ImageLoaderFake
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val IMAGE_URL = "https://shikimori.io/system/animes/original/61316.jpg?175700"
private const val POSTER_FILE_NAME = "anime_notification_poster_61316.jpg"
private const val DISK_CACHE_KEY = "poster-cache-key"
private const val POSTER_BYTES = "poster bytes"

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class PosterLoaderTest {

    private val platformContext = RuntimeEnvironment.getApplication()

    private val cacheDirectory: Path =
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "poster_loader_test_${this.hashCode()}"

    private val copiedPoster: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / POSTER_FILE_NAME

    private var diskCache: DiskCache? = null

    private val image: Image
        get() = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImage()

    @AfterTest
    fun tearDown() {
        diskCache?.shutdown()
        // Windows keeps a handle on a cache file for a moment after the cache is shut down, and
        // a temporary directory left behind is not worth failing a passing test over.
        runCatching { FileSystem.SYSTEM.deleteRecursively(cacheDirectory) }
        FileSystem.SYSTEM.delete(copiedPoster, mustExist = false)
    }

    private fun createLoader(
        result: (ImageRequest) -> ImageResult,
        cache: DiskCache? = null
    ): PosterLoader {
        val imageLoader = ImageLoaderFake(onExecute = result, cache = cache)
        return PosterLoader(
            platformContext = platformContext,
            imageLoaderProvider = { imageLoader }
        )
    }

    private fun createPopulatedDiskCache(): DiskCache {
        val cache = DiskCache.Builder().directory(cacheDirectory).build().also { diskCache = it }
        val editor = assertNotNull(cache.openEditor(DISK_CACHE_KEY))
        cache.fileSystem.write(editor.data) { writeUtf8(POSTER_BYTES) }
        editor.commit()
        return cache
    }

    @Test
    fun aLoadedPosterComesBackAsAnImage() = runTest {
        //Given
        val loader = createLoader({ request -> SuccessResult(image = image, request = request) })

        //When
        val loaded = loader.loadImage(IMAGE_URL)

        //Then
        assertNotNull(loaded)
    }

    @Test
    fun aFailedLoadYieldsNoImage() = runTest {
        //Given
        val loader = createLoader({ request ->
            ErrorResult(image = null, request = request, throwable = Throwable("offline"))
        })

        //When
        val loaded = loader.loadImage(IMAGE_URL)

        //Then
        assertNull(loaded)
    }

    @Test
    fun anAnimeWithNoImageUrlIsNotLoadedAtAll() = runTest {
        //Given
        var executed = false
        val loader = createLoader({ request ->
            executed = true
            SuccessResult(image = image, request = request)
        })

        //When
        val loaded = loader.loadImage(null)

        //Then
        assertNull(loaded)
        assertEquals(false, executed)
    }

    @Test
    fun aCachedPosterIsCopiedOutOfTheCacheUnderItsOwnName() = runTest {
        //Given
        val cache = createPopulatedDiskCache()
        val loader = createLoader(
            result = { request ->
                SuccessResult(image = image, request = request, diskCacheKey = DISK_CACHE_KEY)
            },
            cache = cache
        )

        //When
        val posterFile = loader.loadFile(IMAGE_URL)

        //Then
        assertEquals(copiedPoster, posterFile)
        assertTrue(FileSystem.SYSTEM.exists(copiedPoster))
        assertEquals(
            POSTER_BYTES,
            FileSystem.SYSTEM.read(copiedPoster) { readUtf8() }
        )
    }

    @Test
    fun aPosterThatNeverReachedTheCacheIsNotCopied() = runTest {
        //Given
        val cache = createPopulatedDiskCache()
        val loader = createLoader(
            result = { request -> SuccessResult(image = image, request = request) },
            cache = cache
        )

        //When
        val posterFile = loader.loadFile(IMAGE_URL)

        //Then
        assertNull(posterFile)
        assertEquals(false, FileSystem.SYSTEM.exists(copiedPoster))
    }

    @Test
    fun aLoaderWithNoDiskCacheCopiesNothing() = runTest {
        //Given
        val loader = createLoader({ request ->
            SuccessResult(image = image, request = request, diskCacheKey = DISK_CACHE_KEY)
        })

        //When
        val posterFile = loader.loadFile(IMAGE_URL)

        //Then
        assertNull(posterFile)
    }

    @Test
    fun aFailedLoadCopiesNothing() = runTest {
        //Given
        val cache = createPopulatedDiskCache()
        val loader = createLoader(
            result = { request ->
                ErrorResult(image = null, request = request, throwable = Throwable("offline"))
            },
            cache = cache
        )

        //When
        val posterFile = loader.loadFile(IMAGE_URL)

        //Then
        assertNull(posterFile)
    }

    @Test
    fun anAnimeWithNoImageUrlHasNothingToCopy() = runTest {
        //Given
        val loader = createLoader({ request -> SuccessResult(image = image, request = request) })

        //When
        val posterFile = loader.loadFile(null)

        //Then
        assertNull(posterFile)
    }

    @Test
    fun aCachedPosterThatCannotBeReadIsReportedAsNoPoster() = runTest {
        //Given
        val missingFile = cacheDirectory / "gone.bin"
        val loader = createLoader(
            result = { request ->
                SuccessResult(image = image, request = request, diskCacheKey = DISK_CACHE_KEY)
            },
            cache = DiskCacheFake(missingFile)
        )

        //When
        val posterFile = loader.loadFile(IMAGE_URL)

        //Then
        assertNull(posterFile)
        assertEquals(false, FileSystem.SYSTEM.exists(copiedPoster))
    }
}

/** Hands out a snapshot pointing at a file that is not there, so reading it fails. */
private class DiskCacheFake(private val missingFile: Path) : DiskCache {

    override val size: Long = 0

    override val maxSize: Long = 0

    override val directory: Path = missingFile.parent ?: missingFile

    override val fileSystem: FileSystem = FileSystem.SYSTEM

    override fun openSnapshot(key: String): DiskCache.Snapshot = object : DiskCache.Snapshot {
        override val metadata: Path = missingFile
        override val data: Path = missingFile
        override fun close() = Unit
        override fun closeAndOpenEditor(): DiskCache.Editor? = null
    }

    override fun openEditor(key: String): DiskCache.Editor? = null

    override fun remove(key: String): Boolean = false

    override fun clear() = Unit

    override fun shutdown() = Unit
}
