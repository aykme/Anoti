package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.new_episodes
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.cancellation.CancellationException
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes

class AnimeNotificationManagerImpl(
    private val appContext: Context,
    animeNotificationIntentProvider: AnimeNotificationIntentProvider,
    private val coroutineContextProvider: CoroutineContextProvider
) : AnimeNotificationManager {
    private val tag = "ANIME_NOTIFICATION_MANAGER"

    private val iconColor: Int = SilverTransparent.toArgb()

    // The app-wide loader, so a poster already shown on screen comes from its cache.
    private val imageLoader: ImageLoader by lazy { SingletonImageLoader.get(appContext) }

    private val intent: PendingIntent =
        animeNotificationIntentProvider.getNewEpisodeNotificationIntent(appContext)

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(appContext)

    // The periodic and the one-off update passes can run at the same time, and each id may only
    // be handed out once.
    private val postingMutex = Mutex()

    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from [DEFAULT_SINGLE_ID] to [MAX_SINGLE_ID] */
    private var singleId = DEFAULT_SINGLE_ID

    /** Group ids should be from 0 to 9 */
    private val newEpisodesSummaryId = 0

    @SuppressLint("MissingPermission")
    override suspend fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        withContext(coroutineContextProvider.ioDispatcher) {
            val noDataString = getString(celebrityRes.string.no_data)
            val episodeAiredString = getString(Res.string.episode_aired)
            val singleNotification = buildSingleNotification(
                title = animeName ?: noDataString,
                contentText = "$episodeAiredString: ${airedEpisode ?: noDataString}",
                poster = createPosterImageBitmap(imageUrl)
            )
            val summaryNotification = buildSummaryNotification()

            postingMutex.withLock {
                notificationManager.notify(
                    /* id = */
                    singleId,
                    /* notification = */
                    singleNotification
                )
                changeSingleIdToNext()

                notificationManager.notify(
                    /* id = */
                    newEpisodesSummaryId,
                    /* notification = */
                    summaryNotification
                )
            }
        }
    }

    private fun buildSingleNotification(
        title: String,
        contentText: String,
        poster: Bitmap?
    ): Notification = newEpisodeBuilder()
        .setAutoCancel(true)
        .setContentIntent(intent)
        .setContentTitle(title)
        .setContentText(contentText)
        .setLargeIcon(poster)
        .build()

    private suspend fun buildSummaryNotification(): Notification = newEpisodeBuilder()
        .setGroupSummary(true)
        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        .setStyle(
            NotificationCompat.InboxStyle()
                .setSummaryText(getString(Res.string.new_episodes))
        )
        .build()

    private fun newEpisodeBuilder(): NotificationCompat.Builder = NotificationCompat.Builder(
        /* context = */
        appContext,
        /* channelId = */
        CHANNEL_ID
    )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setGroup(newEpisodesGroupKey)
        .setColor(iconColor)
        .setColorized(true)
        .setSmallIcon(res_R.mipmap.ic_notification)

    private suspend fun createPosterImageBitmap(imageUrl: String?): Bitmap? {
        if (imageUrl == null) return null
        return try {
            // The loader carries no timeout of its own, and this runs inside a bounded
            // WorkManager job, so a stalled host must not hold the pass open.
            val result = withTimeoutOrNull(POSTER_TIMEOUT_MILLIS) {
                imageLoader.execute(ImageRequest.Builder(appContext).data(imageUrl).build())
            }
            when (result) {
                is SuccessResult -> result.image.toBitmap()
                // Coil reports a failed load by returning ErrorResult rather than throwing.
                is ErrorResult -> null.also { Log.e(tag, "${result.throwable}") }
                else -> null.also { Log.e(tag, "Poster load timed out: $imageUrl") }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (
            // Best-effort poster load for the notification; falling back to no image on any
            // other failure is this method's whole purpose.
            @Suppress("TooGenericExceptionCaught") e: Exception
        ) {
            Log.e(tag, "$e")
            null
        }
    }

    private fun changeSingleIdToNext() {
        if (singleId < MAX_SINGLE_ID) {
            singleId++
        } else {
            singleId = DEFAULT_SINGLE_ID
        }
    }

    private companion object {
        private const val DEFAULT_SINGLE_ID = 10
        private const val MAX_SINGLE_ID = 99
        private const val POSTER_TIMEOUT_MILLIS = 10_000L
    }
}
