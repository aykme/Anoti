package com.alekseivinogradov.anime_notification.impl.presentation.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alekseivinogradov.anime_notification.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification.impl.presentation.factory.AnimeNotificationChannelFactory
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

    private val newEpisodesString: String
        get() = resources?.getString(R.string.new_episodes) ?: ""

    private var glideRequestManager: RequestManager? = null
    private var singleBuilder: NotificationCompat.Builder? = null
    private var summaryNotification: Notification? = null
    private var notificationManager: NotificationManagerCompat? = null

    private val summaryNewEpisodesStyle = NotificationCompat.InboxStyle()
    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from "defaultSingleId" to "maxSingleId" */
    private val defaultSingleId = 10
    private val maxSingleId = 99
    private var singleId = defaultSingleId

    /** Group ids should be from 0 to 9 */
    private val newEpisodesSummaryId = 0

    init {
        /**
         * These operations are performed in the "init block"
         * so as not to save the "context" as a variable.
         */
        resources = appContext.resources
        glideRequestManager = Glide.with(appContext)

        singleBuilder = NotificationCompat.Builder(
            /* context = */ appContext,
            /* channelId = */ AnimeNotificationChannelFactory.channelId
        )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(newEpisodesGroupKey)
            .setAutoCancel(true)
            .setSmallIcon(res_R.drawable.ic_notification_96)

        summaryNotification = NotificationCompat.Builder(
            /* context = */ appContext,
            /* channelId = */ AnimeNotificationChannelFactory.channelId
        )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(newEpisodesGroupKey)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setAutoCancel(true)
            .setContentIntent(null)
            .setSmallIcon(res_R.drawable.ic_notification_96)
            .setStyle(summaryNewEpisodesStyle.setSummaryText(newEpisodesString))
            .build()

        notificationManager = NotificationManagerCompat.from(appContext)
    }

    @SuppressLint("MissingPermission")
    override fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        val contentText = "${episodeAiredString}: ${airedEpisode ?: noDataString}"
        singleBuilder!!
            .setContentTitle(animeName ?: noDataString)
            .setContentText(contentText)
            .setLargeIcon(createPosterImageBitmap(imageUrl))
            .setContentIntent(null)

        notificationManager!!.notify(
            /* id = */ singleId,
            /* notification = */ singleBuilder!!.build()
        )
        notificationManager!!.notify(
            /* id = */ newEpisodesSummaryId,
            /* notification = */ summaryNotification!!
        )

        changeSingleIdToNext()
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

    private fun changeSingleIdToNext() {
        if (singleId < maxSingleId) {
            singleId++
        } else {
            singleId = defaultSingleId
        }
    }
}
