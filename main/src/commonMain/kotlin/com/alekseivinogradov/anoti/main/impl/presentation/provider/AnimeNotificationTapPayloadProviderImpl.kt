package com.alekseivinogradov.anoti.main.impl.presentation.provider

import com.alekseivinogradov.anoti.animenotification.external.kmp.api.presentation.provider.AnimeNotificationTapPayloadProvider
import com.alekseivinogradov.anoti.main.impl.presentation.notification.NEW_EPISODE_TAP_TARGET
import com.alekseivinogradov.anoti.main.impl.presentation.notification.NOTIFICATION_TAP_TARGET_KEY
import com.alekseivinogradov.anoti.navigation.kmp.NavRootDeepLink
import me.tatarka.inject.annotations.Inject

/**
 * The "main"-side implementation of [AnimeNotificationTapPayloadProvider]. A tap on a
 * new-episode notification opens [NEW_EPISODE_TAP_TARGET].
 */
@Inject
class AnimeNotificationTapPayloadProviderImpl : AnimeNotificationTapPayloadProvider {
    override fun getNewEpisodeTapPayload(): Map<String, String> = mapOf(
        NOTIFICATION_TAP_TARGET_KEY to NavRootDeepLink.encode(NEW_EPISODE_TAP_TARGET)
    )
}
