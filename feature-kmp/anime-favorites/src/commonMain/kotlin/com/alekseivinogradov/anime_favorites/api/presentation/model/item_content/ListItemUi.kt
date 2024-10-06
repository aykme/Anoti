package com.alekseivinogradov.anime_favorites.api.presentation.model.item_content

import com.alekseivinogradov.anime_base.api.domain.AnimeId

data class ListItemUi(
    val id: AnimeId,
    val imageUrl: String?,
    val score: String,
    val infoType: InfoTypeUi,
    val name: String,
    val availableEpisodesInfo: String,
    val releaseStatus: ReleaseStatusUi,
    val notification: NotificationUi,
    val extraEpisodesInfo: String,
    val episodesWatched: String,
    val mainInfoBackgroundStroke: MainInfoBackgroundStroke
)
