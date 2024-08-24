package com.alekseivinogradov.anime_list.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.alekseivinogradov.anime_list.api.model.list_content.UiListItem

internal class AnimeListDiffUtilCallback : DiffUtil.ItemCallback<UiListItem>() {
    override fun areItemsTheSame(oldItem: UiListItem, newItem: UiListItem): Boolean {
        return oldItem.itemIndex == newItem.itemIndex
    }

    override fun areContentsTheSame(oldItem: UiListItem, newItem: UiListItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: UiListItem, newItem: UiListItem): Any {
        return buildList {
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