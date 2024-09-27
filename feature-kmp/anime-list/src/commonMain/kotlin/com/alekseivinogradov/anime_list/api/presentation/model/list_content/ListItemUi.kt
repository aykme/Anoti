package com.alekseivinogradov.anime_list.api.presentation.model.list_content

import com.alekseivinogradov.anime_base.api.domain.AnimeId

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
