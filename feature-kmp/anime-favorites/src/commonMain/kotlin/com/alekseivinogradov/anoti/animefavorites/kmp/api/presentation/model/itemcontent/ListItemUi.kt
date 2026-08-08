package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

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
