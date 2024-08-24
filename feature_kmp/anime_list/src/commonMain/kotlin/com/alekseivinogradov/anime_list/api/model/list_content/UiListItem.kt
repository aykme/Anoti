package com.alekseivinogradov.anime_list.api.model.list_content

data class UiListItem(
    val itemIndex: Int,
    val imageUrl: String,
    val name: String,
    val episodesInfoType: UiEpisodesInfoType,
    val availableEpisodesInfo: String = "",
    val futureInfo: String = "",
    val score: String,
    val releaseStatus: UiReleaseStatus,
    val notification: UiNotification
)