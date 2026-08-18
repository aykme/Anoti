package com.alekseivinogradov.anoti.navigation.kmp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A screen reachable from the app's root navigation stack. */
@Serializable
sealed interface NavRootConfig {

    /** The anime list screen — the app's start destination. */
    @Serializable
    @SerialName("AnimeList")
    data object AnimeList : NavRootConfig

    /** The anime favorites screen. */
    @Serializable
    @SerialName("AnimeFavorites")
    data object AnimeFavorites : NavRootConfig
}
