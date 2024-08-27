package com.alekseivinogradov.anime_list.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi

internal class AnimeListDiffUtilCallback : DiffUtil.ItemCallback<ListItemUi>() {
    override fun areItemsTheSame(oldItem: ListItemUi, newItem: ListItemUi): Boolean {
        return oldItem.itemIndex == newItem.itemIndex
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
            if (oldItem.availableEpisodesInfo != newItem.availableEpisodesInfo) {
                add(AnimeListPayload.AvailableEpisodesInfoChange(newItem.availableEpisodesInfo))
            }
            if (oldItem.futureInfo != newItem.futureInfo) {
                add(AnimeListPayload.FutureInfoChange(newItem.futureInfo))
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