package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * One anime list item, ready for display.
 *
 * @param id anime's id.
 * @param name title.
 * @param imageUrl cover image, or null if unavailable.
 * @param episodesInfoType which episode-info variant to show.
 * @param episodesAired number of episodes aired so far, or null if unknown.
 * @param episodesTotal total number of episodes, or null if unknown.
 * @param nextEpisodeAt air date/time of the next episode, or null if none is scheduled.
 * @param airedOn date the anime started airing, or null if unknown.
 * @param releasedOn date the anime finished airing, or null if unknown/not yet released.
 * @param score community score, formatted for display.
 * @param releaseStatus current release status.
 * @param notification whether the "new episode" notification is enabled.
 */
data class ListItemUi(
    val id: AnimeId,
    val name: String,
    val imageUrl: String?,
    val episodesInfoType: EpisodesInfoTypeUi,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val nextEpisodeAt: String?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: String,
    val releaseStatus: ReleaseStatusUi,
    val notification: NotificationUi
)
