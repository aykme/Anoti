package com.alekseivinogradov.anoti.animenotification.external.kmp.api.presentation.provider

/**
 * Builds what a new-episode notification carries to the app, so a tap opens the screen it
 * names. Implemented by the module that owns the screens ("main"), so this module and its
 * consumers need no compile-time dependency on it.
 */
interface AnimeNotificationTapPayloadProvider {

    /** The entries a new-episode notification carries for its tap. */
    fun getNewEpisodeTapPayload(): Map<String, String>
}
