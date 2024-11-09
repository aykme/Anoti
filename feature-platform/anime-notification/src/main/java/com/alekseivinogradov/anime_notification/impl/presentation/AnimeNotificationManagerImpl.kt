package com.alekseivinogradov.anime_notification.impl.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alekseivinogradov.anime_notification.api.domain.notification_manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification_platform.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kotlin.coroutines.cancellation.CancellationException
import com.alekseivinogradov.res.R as res_R

class AnimeNotificationManagerImpl(
    appContext: Context
) : AnimeNotificationManager {

    private var resources: Resources? = null

    private val episodeAiredString: String
        get() = resources?.getString(R.string.episode_aired) ?: ""

    private val noDataString: String
        get() = resources?.getString(R.string.no_data) ?: ""

    private var glideRequestManager: RequestManager? = null
    private var baseSingleNewEpisodeBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManagerCompat? = null

    private val singleNewEpisodeStyle = NotificationCompat.BigTextStyle()
    private val newEpisodeGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from 10 to Int.MaxValue */
    private val defaultSingleId = 10
    private var singleId = defaultSingleId

    /** Group ids should be from 0 to 9 */
    private val newEpisodeSummaryId = 0

    init {
        /**
         * These operations are performed in the "init block"
         * so as not to save the "context" as a variable.
         */
        resources = appContext.resources
        glideRequestManager = Glide.with(appContext)

        baseSingleNewEpisodeBuilder = NotificationCompat.Builder(
            /* context = */ appContext,
            /* channelId = */ AnimeNotificationManager.channelId
        )
            .setSmallIcon(res_R.drawable.ic_notification_96)

        notificationManager = NotificationManagerCompat.from(appContext)
    }

    @SuppressLint("MissingPermission")
    override fun makeNewEpisodeNotification(
        animeName: String?,
        episodesAired: Int?,
        imageUrl: String?
    ) {
        val contentText = "${episodeAiredString}: ${episodesAired ?: noDataString}"
        baseSingleNewEpisodeBuilder!!
            .setContentTitle(animeName ?: noDataString)
            .setStyle(singleNewEpisodeStyle.bigText(contentText))
            .setLargeIcon(createPosterImageBitmap(imageUrl))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(newEpisodeGroupKey)
            .setAutoCancel(true)
            .setContentIntent(null)

        notificationManager!!.notify(
            /* id = */ singleId,
            /* notification = */ baseSingleNewEpisodeBuilder!!.build()
        )

        if (singleId < Int.MAX_VALUE) {
            singleId++
        } else {
            singleId == defaultSingleId
        }
    }

    private fun createPosterImageBitmap(imageUrl: String?): Bitmap? {
        return try {
            glideRequestManager!!
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
