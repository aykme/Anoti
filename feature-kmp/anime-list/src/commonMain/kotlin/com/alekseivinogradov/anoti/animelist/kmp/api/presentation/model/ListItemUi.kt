package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

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
