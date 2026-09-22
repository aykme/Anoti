package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import coil3.ComponentRegistry
import coil3.Image
import coil3.ImageLoader
import coil3.asImage
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.getString
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.coroutines.CoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes

private const val ANIME_NAME = "Frieren"
private const val AIRED_EPISODE = 7
private const val IMAGE_URL = "https://shikimori.io/system/animes/original/1.jpg"
private const val FIRST_SINGLE_ID = 10
private const val LAST_SINGLE_ID = 99
private const val SUMMARY_ID = 0
private const val SINGLE_ID_COUNT = LAST_SINGLE_ID - FIRST_SINGLE_ID + 1

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AnimeNotificationManagerImplTest {

    private val appContext: Context = RuntimeEnvironment.getApplication()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val coroutineContextProvider = object : CoroutineContextProviderBase() {
        override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        override val ioDispatcher: CoroutineContext = testDispatcher
    }

    private val notificationManager =
        appContext.getSystemService(NotificationManager::class.java)

    private val poster: Image
        get() = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImage()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createManager(loadsPoster: Boolean = true): AnimeNotificationManager {
        val imageLoader = FakeImageLoader { request: ImageRequest ->
            if (loadsPoster) {
                SuccessResult(image = poster, request = request)
            } else {
                ErrorResult(image = null, request = request, throwable = Throwable("no poster"))
            }
        }
        return AnimeNotificationManagerImpl(
            appContext = appContext,
            animeNotificationIntentProvider = FakeIntentProvider,
            coroutineContextProvider = coroutineContextProvider,
            posterLoader = PosterLoader(
                platformContext = appContext,
                imageLoaderProvider = { imageLoader }
            )
        )
    }

    private fun notificationWithId(id: Int): Notification? =
        shadowOf(notificationManager).getNotification(id)

    private fun titleOf(notification: Notification): String? =
        notification.extras.getString(Notification.EXTRA_TITLE)

    private fun textOf(notification: Notification): String? =
        notification.extras.getString(Notification.EXTRA_TEXT)

    @Test
    fun theNotificationNamesTheAnimeAndTheEpisodeThatAired() = runTest {
        //Given
        val manager = createManager()
        val episodeAired = getString(Res.string.episode_aired)

        //When
        manager.makeNewEpisodeNotification(
            animeName = ANIME_NAME,
            airedEpisode = AIRED_EPISODE,
            imageUrl = IMAGE_URL
        )

        //Then
        val notification = assertNotNull(notificationWithId(FIRST_SINGLE_ID))
        assertEquals(ANIME_NAME, titleOf(notification))
        assertEquals("$episodeAired: $AIRED_EPISODE", textOf(notification))
    }

    @Test
    fun aSummaryNotificationIsPostedAlongsideTheEpisodeItself() = runTest {
        //Given
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification(
            animeName = ANIME_NAME,
            airedEpisode = AIRED_EPISODE,
            imageUrl = IMAGE_URL
        )

        //Then
        val summary = assertNotNull(notificationWithId(SUMMARY_ID))
        assertTrue(summary.flags and Notification.FLAG_GROUP_SUMMARY != 0)
        assertEquals(2, shadowOf(notificationManager).size())
    }

    @Test
    fun bothNotificationsGoToTheAppsOwnChannelAndGroup() = runTest {
        //Given
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification(
            animeName = ANIME_NAME,
            airedEpisode = AIRED_EPISODE,
            imageUrl = IMAGE_URL
        )

        //Then
        val single = assertNotNull(notificationWithId(FIRST_SINGLE_ID))
        val summary = assertNotNull(notificationWithId(SUMMARY_ID))
        assertEquals(CHANNEL_ID, single.channelId)
        assertEquals(CHANNEL_ID, summary.channelId)
        assertNotNull(single.group)
        assertEquals(single.group, summary.group)
    }

    @Test
    fun anUnknownAnimeNameAndEpisodeBothFallBackToTheNoDataText() = runTest {
        //Given
        val manager = createManager()
        val noData = getString(celebrityRes.string.no_data)
        val episodeAired = getString(Res.string.episode_aired)

        //When
        manager.makeNewEpisodeNotification(
            animeName = null,
            airedEpisode = null,
            imageUrl = IMAGE_URL
        )

        //Then
        val notification = assertNotNull(notificationWithId(FIRST_SINGLE_ID))
        assertEquals(noData, titleOf(notification))
        assertEquals("$episodeAired: $noData", textOf(notification))
    }

    @Test
    fun theLoadedPosterBecomesTheNotificationsLargeIcon() = runTest {
        //Given
        val manager = createManager(loadsPoster = true)

        //When
        manager.makeNewEpisodeNotification(
            animeName = ANIME_NAME,
            airedEpisode = AIRED_EPISODE,
            imageUrl = IMAGE_URL
        )

        //Then
        val notification = assertNotNull(notificationWithId(FIRST_SINGLE_ID))
        assertNotNull(notification.getLargeIcon())
    }

    @Test
    fun aPosterThatFailsToLoadStillLeavesTheNotificationPosted() = runTest {
        //Given
        val manager = createManager(loadsPoster = false)

        //When
        manager.makeNewEpisodeNotification(
            animeName = ANIME_NAME,
            airedEpisode = AIRED_EPISODE,
            imageUrl = IMAGE_URL
        )

        //Then
        val notification = assertNotNull(notificationWithId(FIRST_SINGLE_ID))
        assertEquals(ANIME_NAME, titleOf(notification))
        assertNull(notification.getLargeIcon())
    }

    @Test
    fun aMissingImageUrlSkipsThePosterWithoutSkippingTheNotification() = runTest {
        //Given
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification(
            animeName = ANIME_NAME,
            airedEpisode = AIRED_EPISODE,
            imageUrl = null
        )

        //Then
        val notification = assertNotNull(notificationWithId(FIRST_SINGLE_ID))
        assertNull(notification.getLargeIcon())
    }

    @Test
    fun eachEpisodeGetsItsOwnNotificationInsteadOfReplacingTheLastOne() = runTest {
        //Given
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification(ANIME_NAME, AIRED_EPISODE, IMAGE_URL)
        manager.makeNewEpisodeNotification("Bleach", AIRED_EPISODE + 1, IMAGE_URL)

        //Then
        assertEquals(ANIME_NAME, titleOf(assertNotNull(notificationWithId(FIRST_SINGLE_ID))))
        assertEquals("Bleach", titleOf(assertNotNull(notificationWithId(FIRST_SINGLE_ID + 1))))
    }

    @Test
    fun theNotificationIdWrapsBackAroundOnceTheLastOneIsUsed() = runTest {
        //Given
        val manager = createManager()
        repeat(SINGLE_ID_COUNT) { number: Int ->
            manager.makeNewEpisodeNotification("Anime $number", number, IMAGE_URL)
        }
        val postedBeforeWrapping = shadowOf(notificationManager).size()

        //When
        manager.makeNewEpisodeNotification("After the wrap", AIRED_EPISODE, IMAGE_URL)

        //Then
        assertEquals(SINGLE_ID_COUNT + 1, postedBeforeWrapping)
        assertEquals(postedBeforeWrapping, shadowOf(notificationManager).size())
        assertEquals(
            "After the wrap",
            titleOf(assertNotNull(notificationWithId(FIRST_SINGLE_ID)))
        )
    }
}

private object FakeIntentProvider : AnimeNotificationIntentProvider {
    override fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent =
        PendingIntent.getActivity(
            appContext,
            0,
            Intent(),
            PendingIntent.FLAG_IMMUTABLE
        )
}

private class FakeImageLoader(
    private val onExecute: (ImageRequest) -> ImageResult
) : ImageLoader {

    override val defaults: ImageRequest.Defaults = ImageRequest.Defaults.DEFAULT

    override val components: ComponentRegistry = ComponentRegistry()

    override val memoryCache: MemoryCache? = null

    override val diskCache: DiskCache? = null

    override suspend fun execute(request: ImageRequest): ImageResult = onExecute(request)

    override fun enqueue(request: ImageRequest): Disposable =
        error("not used in AnimeNotificationManagerImplTest")

    override fun newBuilder(): ImageLoader.Builder =
        error("not used in AnimeNotificationManagerImplTest")

    override fun shutdown() = Unit
}
