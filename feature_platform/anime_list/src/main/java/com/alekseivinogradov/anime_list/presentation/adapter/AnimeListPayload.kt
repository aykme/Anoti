package com.alekseivinogradov.anime_list.presentation.adapter

import com.alekseivinogradov.anime_list.api.presentation.model.list_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ReleaseStatusUi

internal sealed interface AnimeListPayload {
    data class ImageUrlChange(val imageUrl: String?) : AnimeListPayload
    data class NameChange(val name: String) : AnimeListPayload
    data class EpisodesInfoTypeChange(val episodesInfoType: EpisodesInfoTypeUi) : AnimeListPayload
    data class AvailableEpisodesInfoChange(val availableEpisodesInfo: String) : AnimeListPayload
    data class ExtraEpisodesInfoChange(val extraEpisodesInfo: String) : AnimeListPayload
    data class ScoreChange(val score: String) : AnimeListPayload
    data class ReleaseStatusChange(val releaseStatus: ReleaseStatusUi) : AnimeListPayload
    data class NotificationChange(val notification: NotificationUi) : AnimeListPayload
}