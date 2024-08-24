package com.alekseivinogradov.anime_list.api.model.list_content

data class UiListContent(
    val name: String,
    val episodesInfoType: EpisodesInfoType,
    val currentEpisodesInfo: String = "",
    val futureInfo: String = "",
    val score: String,
    val releaseStatus: ReleaseStatus,
    val notification: Notification
)