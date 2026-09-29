package com.alekseivinogradov.anoti.navigation.kmp

import kotlinx.serialization.json.Json

/**
 * The text form of a [NavRootConfig] that a notification carries to the screen host, so the app
 * can open on the screen the notification names.
 */
object NavRootDeepLink {

    /** Writes [config] as the payload a notification carries. */
    fun encode(config: NavRootConfig): String =
        Json.encodeToString(NavRootConfig.serializer(), config)

    /**
     * Reads the screen [payload] names, or `null` when there is no payload or it names no known
     * screen. The payload can come from any app, so nothing it holds is allowed to throw.
     */
    fun decode(payload: String?): NavRootConfig? = payload?.let { encoded: String ->
        runCatching { Json.decodeFromString(NavRootConfig.serializer(), encoded) }.getOrNull()
    }
}
