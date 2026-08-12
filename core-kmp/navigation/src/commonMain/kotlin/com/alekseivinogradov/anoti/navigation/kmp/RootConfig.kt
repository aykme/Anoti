package com.alekseivinogradov.anoti.navigation.kmp

import kotlinx.serialization.Serializable

/** A screen reachable from the app's root navigation stack. */
@Serializable
sealed interface RootConfig {

    /** The anime list screen — the app's start destination. */
    @Serializable
    data object AnimeList : RootConfig

    /** The anime favorites screen. */
    @Serializable
    data object AnimeFavorites : RootConfig
}
