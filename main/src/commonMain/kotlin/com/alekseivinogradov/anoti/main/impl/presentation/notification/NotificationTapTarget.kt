package com.alekseivinogradov.anoti.main.impl.presentation.notification

import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.alekseivinogradov.anoti.navigation.kmp.NavRootDeepLink

/** The key under which a new-episode notification carries the screen its tap opens. */
internal const val NOTIFICATION_TAP_TARGET_KEY = "deep_link_target"

/** The screen a tap on a new-episode notification opens, on every platform. */
internal val NEW_EPISODE_TAP_TARGET: NavRootConfig = NavRootConfig.AnimeFavorites

/**
 * Reads the screen a tapped notification names from its [payload], or `null` when it names none
 * this app knows.
 */
internal fun notificationTapTarget(payload: Map<*, *>): NavRootConfig? =
    NavRootDeepLink.decode(payload = payload[NOTIFICATION_TAP_TARGET_KEY] as? String)
