package com.alekseivinogradov.anoti.main.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.main.impl.presentation.notification.NEW_EPISODE_TAP_TARGET
import com.alekseivinogradov.anoti.navigation.kmp.NavRootDeepLink
import me.tatarka.inject.annotations.Inject

/**
 * The "main"-side implementation of [AnimeNotificationIntentProvider]. Lives here because it is
 * the only module that owns both the root navigation component and the notification's target
 * activity.
 *
 * `NEW_TASK`, `CLEAR_TOP` and `SINGLE_TOP` bring the task forward and close whatever this app
 * opened above [MainActivity], such as the notification settings. The running activity then gets
 * the tap in `onNewIntent`. With no task, a new one starts and the activity reads the target at
 * launch.
 */
@Inject
class AnimeNotificationIntentProviderImpl : AnimeNotificationIntentProvider {
    override fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(
                MainActivity.EXTRA_DEEP_LINK_TARGET,
                NavRootDeepLink.encode(NEW_EPISODE_TAP_TARGET)
            )
        }
        return PendingIntent.getActivity(
            /* context = */
            appContext,
            /* requestCode = */
            REQUEST_CODE,
            /* intent = */
            intent,
            /* flags = */
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE = 0
    }
}
