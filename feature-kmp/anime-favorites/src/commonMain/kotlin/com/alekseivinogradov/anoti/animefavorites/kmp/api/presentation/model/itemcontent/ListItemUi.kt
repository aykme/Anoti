package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * One anime in the favorites list, ready for display.
 *
 * @param id anime's id.
 * @param imageUrl cover image, or null if unavailable.
 * @param score community score, formatted for display.
 * @param infoType which episode-info variant to show.
 * @param name title.
 * @param availableEpisodesInfo episode count/progress, formatted for display.
 * @param releaseStatus current release status.
 * @param notification whether the "new episode" notification is enabled.
 * @param extraEpisodesInfo extra episode details shown when [infoType] is [InfoTypeUi.EXTRA],
 * or null if unavailable.
 * @param episodesViewed number of episodes the user has marked as viewed, formatted for display.
 * @param isNewEpisode whether a newly aired episode hasn't been viewed yet.
 */
data class ListItemUi(
    val id: AnimeId,
    val imageUrl: String?,
    val score: String,
    val infoType: InfoTypeUi,
    val name: String,
    val availableEpisodesInfo: String,
    val releaseStatus: ReleaseStatusUi,
    val notification: NotificationUi,
    val extraEpisodesInfo: String?,
    val episodesViewed: String,
    val isNewEpisode: Boolean
)
