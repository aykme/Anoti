package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory

import android.app.NotificationChannel
import android.app.NotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.anime_notification_channel
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.anime_notification_channel_description
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class AnimeNotificationChannelFactory @Inject constructor(
    private val coroutineContextProvider: CoroutineContextProvider
) {

    fun create(): NotificationChannel {
        val name = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.anime_notification_channel)
        }
        val channelDescription = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.anime_notification_channel_description)
        }
        return NotificationChannel(
            /* id = */ channelId,
            /* name = */ name,
            /* importance = */ NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = channelDescription
            enableVibration(true)
        }
    }
}

const val channelId = "ANIME_NOTIFICATION_CHANNEL_ID"
