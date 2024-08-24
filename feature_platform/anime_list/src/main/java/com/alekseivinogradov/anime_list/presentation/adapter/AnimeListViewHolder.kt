package com.alekseivinogradov.anime_list.presentation.adapter

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anime_list.R
import com.alekseivinogradov.anime_list.api.model.list_content.UiEpisodesInfoType
import com.alekseivinogradov.anime_list.api.model.list_content.UiListItem
import com.alekseivinogradov.anime_list.api.model.list_content.UiNotification
import com.alekseivinogradov.anime_list.api.model.list_content.UiReleaseStatus
import com.alekseivinogradov.anime_list.databinding.ItemAnimeListBinding
import com.alekseivinogradov.theme.R as theme_R

internal class AnimeListViewHolder(
    private val binding: ItemAnimeListBinding,
    private val episodesInfoClickListener: View.OnClickListener,
    private val notificationButtonClickListener: View.OnClickListener
) :
    RecyclerView.ViewHolder(binding.root) {

    private val context = binding.root.context

    init {
        binding.availableEpisodesInfoButton.setOnClickListener(episodesInfoClickListener)
        binding.futureInfoButton.setOnClickListener(episodesInfoClickListener)
        binding.notificationButton.setOnClickListener(notificationButtonClickListener)
    }

    internal fun bindWithPayload(payload: AnimeListPayload) {
        when (payload) {
            is AnimeListPayload.NameChange -> {
                bindName(payload.name)
            }

            is AnimeListPayload.EpisodesInfoTypeChange -> {
                bindEpisodesInfoType(payload.episodesInfoType)
            }

            is AnimeListPayload.AvailableEpisodesInfoChange -> {
                bindAvailableEpisodesInfo(payload.availableEpisodesInfo)
            }

            is AnimeListPayload.FutureInfoChange -> {
                bindFutureInfo(payload.futureInfo)
            }

            is AnimeListPayload.ScoreChange -> {
                bindScore(payload.score)
            }

            is AnimeListPayload.ReleaseStatusChange -> {
                bindReleaseStatus(payload.releaseStatus)
            }

            is AnimeListPayload.NotificationChange -> {
                bindNotification(payload.notification)
            }
        }
    }

    internal fun bind(item: UiListItem) {
        bindCommonFieldsVisibility()
        bindName(item.name)
        bindEpisodesInfoType(item.episodesInfoType)
        bindAvailableEpisodesInfo(item.availableEpisodesInfo)
        bindFutureInfo(item.futureInfo)
        bindScore(item.score)
        bindReleaseStatus(item.releaseStatus)
        bindNotification(item.notification)
    }

    private fun bindCommonFieldsVisibility() {
        with(binding) {
            nameText.isVisible = true
            scoreImage.isVisible = true
            scoreText.isVisible = true
            verticalDividerAfterScore.isVisible = true
            releasedStatusText.isVisible = true
            verticalDividerAfterStatus.isVisible = true
            notificationButton.isVisible = true
        }
    }

    private fun bindName(name: String) {
        binding.nameText.text = name
    }

    private fun bindEpisodesInfoType(episodesInfoType: UiEpisodesInfoType) {
        with(binding) {
            when (episodesInfoType) {
                UiEpisodesInfoType.CURRENT -> {
                    futureInfoText.isVisible = false
                    availableEpisodesInfoButton.isVisible = false
                    availableEpisodesInfoText.isVisible = true
                    futureInfoButton.isVisible = true
                }

                UiEpisodesInfoType.FUTURE -> {
                    availableEpisodesInfoText.isVisible = false
                    futureInfoButton.isVisible = false
                    futureInfoText.isVisible = true
                    availableEpisodesInfoButton.isVisible = true
                }
            }
        }
    }

    private fun bindAvailableEpisodesInfo(availableEpisodesInfo: String) {
        binding.availableEpisodesInfoText.text = availableEpisodesInfo
    }

    private fun bindFutureInfo(futureInfo: String) {
        binding.futureInfoText.text = futureInfo
    }

    private fun bindScore(score: String) {
        binding.scoreText.text = score
    }

    private fun bindReleaseStatus(releaseStatus: UiReleaseStatus) {
        with(binding.releasedStatusText) {
            when (releaseStatus) {
                UiReleaseStatus.ONGOING -> {
                    text = context.applicationContext.getString(R.string.ongoings)
                    setTextColor(context.getColor(theme_R.color.green))
                }

                UiReleaseStatus.ANNOUNCED -> {
                    text = context.applicationContext.getString(R.string.announced)
                    setTextColor(context.getColor(theme_R.color.purple_200))
                }

                UiReleaseStatus.RELEASED -> {
                    text = context.applicationContext.getString(R.string.released)
                    setTextColor(context.getColor(theme_R.color.pink))
                }
            }
        }
    }

    private fun bindNotification(notification: UiNotification) {
        with(binding.notificationButton) {
            when (notification) {
                UiNotification.ENABLED -> {
                    setImageDrawable(
                        ContextCompat.getDrawable(
                            context.applicationContext,
                            R.drawable.ic_notifications_on_40
                        )
                    )
                    backgroundTintList = ColorStateList.valueOf(
                        context.getColor(theme_R.color.green)
                    )
                    rippleColor = context.getColor(theme_R.color.green)
                    contentDescription = resources.getString(
                        R.string.notifications_turn_on
                    )
                }

                UiNotification.DISABLED -> {
                    setImageDrawable(
                        ContextCompat.getDrawable(
                            context.applicationContext,
                            R.drawable.ic_notifications_off_40
                        )
                    )
                    backgroundTintList = ColorStateList.valueOf(
                        context.getColor(theme_R.color.pink)
                    )
                    rippleColor = context.getColor(theme_R.color.pink)
                    contentDescription = resources.getString(
                        R.string.notifications_turn_off
                    )
                }
            }
        }
    }
}