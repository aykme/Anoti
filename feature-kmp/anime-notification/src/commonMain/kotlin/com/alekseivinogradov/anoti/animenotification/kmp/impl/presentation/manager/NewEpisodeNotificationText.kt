package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager

import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import org.jetbrains.compose.resources.getString
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes

/**
 * What a new-episode notification says.
 *
 * @param title the name of the anime the episode belongs to.
 * @param body the line naming the episode that aired.
 */
internal data class NewEpisodeNotificationText(val title: String, val body: String)

/**
 * The wording for [animeName]'s newly aired [airedEpisode]. Anything the update pass could not
 * read falls back to the shared "no data" text, so a notification never arrives blank.
 */
internal suspend fun newEpisodeNotificationText(
    animeName: String?,
    airedEpisode: Int?
): NewEpisodeNotificationText {
    val noData = getString(celebrityRes.string.no_data)
    return NewEpisodeNotificationText(
        title = animeName ?: noData,
        body = "${getString(Res.string.episode_aired)}: ${airedEpisode ?: noData}"
    )
}
