package com.alekseivinogradov.anime_list.api.presentation.model.list_content

import com.alekseivinogradov.anime_base.api.domain.AnimeId

data class ListItemUi(
    val id: AnimeId,
    val imageUrl: String?,
    val name: String,
    val episodesInfoType: EpisodesInfoTypeUi,
    val availableEpisodesInfo: String,
    val extraEpisodesInfo: String?,
    val score: String,
    val releaseStatus: ReleaseStatusUi,
    val notification: NotificationUi
)
