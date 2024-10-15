package com.alekseivinogradov.anime_favorites.impl.presentation.adapter

import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.InfoTypeUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.NotificationUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ReleaseStatusUi

internal sealed interface AnimeFavoritesPayload {
    data class ImageUrlChange(val imageUrl: String?) : AnimeFavoritesPayload
    data class ScoreChange(val score: String) : AnimeFavoritesPayload
    data class InfoTypeChange(val infoType: InfoTypeUi) : AnimeFavoritesPayload
    data class NameChange(val name: String) : AnimeFavoritesPayload
    data class AvailableEpisodesInfoChange(
        val availableEpisodesInfo: String
    ) : AnimeFavoritesPayload

    data class ReleaseStatusChange(val releaseStatus: ReleaseStatusUi) : AnimeFavoritesPayload
    data class NotificationChange(val notification: NotificationUi) : AnimeFavoritesPayload
    data class ExtraEpisodesInfoChange(
        val extraEpisodesInfo: String?,
        val releaseStatus: ReleaseStatusUi
    ) : AnimeFavoritesPayload

    data class EpisodesViewedChange(val episodesViewed: String) : AnimeFavoritesPayload
    data class NewEpisodeStatusChange(val isNewEpisode: Boolean) : AnimeFavoritesPayload
}
