package com.alekseivinogradov.anime_list.api.presentation.model.list_content

data class ListItemUi(
    val itemIndex: Int,
    val imageUrl: String?,
    val name: String,
    val episodesInfoType: EpisodesInfoTypeUi,
    val availableEpisodesInfo: String,
    val extraEpisodesInfo: String?,
    val score: String,
    val releaseStatus: ReleaseStatusUi,
    val notification: NotificationUi
)
