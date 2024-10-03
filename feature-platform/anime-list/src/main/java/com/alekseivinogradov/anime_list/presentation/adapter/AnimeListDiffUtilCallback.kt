package com.alekseivinogradov.anime_list.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.ListItemUi

internal class AnimeListDiffUtilCallback : DiffUtil.ItemCallback<ListItemUi>() {
    override fun areItemsTheSame(oldItem: ListItemUi, newItem: ListItemUi): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItemUi, newItem: ListItemUi): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: ListItemUi, newItem: ListItemUi): Any {
        return buildList {
            if (oldItem.imageUrl != newItem.imageUrl) {
                add(AnimeListPayload.ImageUrlChange(newItem.imageUrl))
            }
            if (oldItem.name != newItem.name) {
                add(AnimeListPayload.NameChange(newItem.name))
            }
            if (oldItem.episodesInfoType != newItem.episodesInfoType) {
                add(AnimeListPayload.EpisodesInfoTypeChange(newItem.episodesInfoType))
            }
            if (
                oldItem.episodesAired != newItem.episodesAired ||
                oldItem.episodesTotal != newItem.episodesTotal
            ) {
                add(
                    AnimeListPayload.AvailableEpisodesInfoChange(
                        episodesAired = newItem.episodesAired,
                        episodesTotal = newItem.episodesTotal,
                        releaseStatus = newItem.releaseStatus
                    )
                )
            }
            if (
                oldItem.nextEpisodeAt != newItem.nextEpisodeAt ||
                oldItem.airedOn != newItem.airedOn ||
                oldItem.releasedOn != newItem.releasedOn ||
                oldItem.releaseStatus != newItem.releaseStatus
            ) {
                add(
                    AnimeListPayload.ExtraEpisodesInfoChange(
                        nextEpisodeAt = newItem.nextEpisodeAt,
                        airedOn = newItem.airedOn,
                        releasedOn = newItem.releasedOn,
                        releaseStatus = newItem.releaseStatus
                    )
                )
            }
            if (oldItem.score != newItem.score) {
                add(AnimeListPayload.ScoreChange(newItem.score))
            }
            if (oldItem.releaseStatus != newItem.releaseStatus) {
                add(AnimeListPayload.ReleaseStatusChange(newItem.releaseStatus))
            }
            if (oldItem.notification != newItem.notification) {
                add(AnimeListPayload.NotificationChange(newItem.notification))
            }
        }
    }
}
