package com.alekseivinogradov.anime_list.presentation.adapter

import com.alekseivinogradov.anime_list.api.model.list_content.UiEpisodesInfoType
import com.alekseivinogradov.anime_list.api.model.list_content.UiNotification
import com.alekseivinogradov.anime_list.api.model.list_content.UiReleaseStatus

internal sealed interface AnimeListPayload {
    data class ImageUrlChange(val imageUrl: String) : AnimeListPayload
    data class NameChange(val name: String) : AnimeListPayload
    data class EpisodesInfoTypeChange(val episodesInfoType: UiEpisodesInfoType) : AnimeListPayload
    data class AvailableEpisodesInfoChange(val availableEpisodesInfo: String) : AnimeListPayload
    data class FutureInfoChange(val futureInfo: String) : AnimeListPayload
    data class ScoreChange(val score: String) : AnimeListPayload
    data class ReleaseStatusChange(val releaseStatus: UiReleaseStatus) : AnimeListPayload
    data class NotificationChange(val notification: UiNotification) : AnimeListPayload
}