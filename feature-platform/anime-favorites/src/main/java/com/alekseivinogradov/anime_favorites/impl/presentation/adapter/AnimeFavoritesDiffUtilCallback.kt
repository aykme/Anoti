package com.alekseivinogradov.anime_favorites.impl.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi

internal class AnimeFavoritesDiffUtilCallback : DiffUtil.ItemCallback<ListItemUi>() {
    override fun areItemsTheSame(oldItem: ListItemUi, newItem: ListItemUi): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItemUi, newItem: ListItemUi): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: ListItemUi, newItem: ListItemUi): Any {
        return buildList {
            if (oldItem.imageUrl != newItem.imageUrl) {
                add(AnimeFavoritesPayload.ImageUrlChange(newItem.imageUrl))
            }
            if (oldItem.score != newItem.score) {
                add(AnimeFavoritesPayload.ScoreChange(newItem.score))
            }
            if (oldItem.infoType != newItem.infoType) {
                add(AnimeFavoritesPayload.InfoTypeChange(newItem.infoType))
            }
            if (oldItem.name != newItem.name) {
                add(AnimeFavoritesPayload.NameChange(newItem.name))
            }
            if (oldItem.availableEpisodesInfo != newItem.availableEpisodesInfo) {
                add(
                    AnimeFavoritesPayload.AvailableEpisodesInfoChange(
                        availableEpisodesInfo = newItem.availableEpisodesInfo
                    )
                )
            }
            if (oldItem.releaseStatus != newItem.releaseStatus) {
                add(AnimeFavoritesPayload.ReleaseStatusChange(newItem.releaseStatus))
            }
            if (oldItem.notification != newItem.notification) {
                add(AnimeFavoritesPayload.NotificationChange(newItem.notification))
            }
            if (oldItem.extraEpisodesInfo != newItem.extraEpisodesInfo) {
                add(
                    AnimeFavoritesPayload.ExtraEpisodesInfoChange(
                        extraEpisodesInfo = newItem.extraEpisodesInfo,
                        releaseStatus = newItem.releaseStatus
                    )
                )
            }
            if (oldItem.episodesViewed != newItem.episodesViewed) {
                add(AnimeFavoritesPayload.EpisodesViewedChange(newItem.episodesViewed))
            }
            if (oldItem.isNewEpisode != newItem.isNewEpisode) {
                add(AnimeFavoritesPayload.NewEpisodeStatusChange(newItem.isNewEpisode))
            }
        }
    }
}
