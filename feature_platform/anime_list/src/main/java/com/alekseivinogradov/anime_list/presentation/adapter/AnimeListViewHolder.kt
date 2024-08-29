package com.alekseivinogradov.anime_list.presentation.adapter

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anime_list.R
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ReleaseStatusUi
import com.alekseivinogradov.anime_list.databinding.ItemAnimeListBinding
import com.bumptech.glide.Glide
import com.alekseivinogradov.theme.R as theme_R

internal class AnimeListViewHolder(
    private val binding: ItemAnimeListBinding,
    private val episodesInfoClickCallback: (Int) -> Unit,
    private val notificationClickCallback: (Int) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    private val context
        get() = binding.root.context

    private val transparentColor
        get() = context.getColor(theme_R.color.transparent)

    init {
        bindCommonFields()
    }

    internal fun bindWithPayload(payload: AnimeListPayload) {
        when (payload) {
            is AnimeListPayload.ImageUrlChange -> {
                bindImageUrl(payload.imageUrl)
            }

            is AnimeListPayload.NameChange -> {
                bindName(payload.name)
            }

            is AnimeListPayload.EpisodesInfoTypeChange -> {
                bindEpisodesInfoType(payload.episodesInfoType)
            }

            is AnimeListPayload.AvailableEpisodesInfoChange -> {
                bindAvailableEpisodesInfo(payload.availableEpisodesInfo)
            }

            is AnimeListPayload.ExtraEpisodesInfoChange -> {
                bindExtraEpisodesInfo(payload.extraEpisodesInfo)
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

    internal fun bind(item: ListItemUi) {
        bindCommonFields()
        bindImageUrl(item.imageUrl)
        bindName(item.name)
        bindEpisodesInfoType(item.episodesInfoType)
        bindAvailableEpisodesInfo(item.availableEpisodesInfo)
        bindExtraEpisodesInfo(item.extraEpisodesInfo)
        bindScore(item.score)
        bindReleaseStatus(item.releaseStatus)
        bindNotification(item.notification)
        binding.availableEpisodesInfoButton.setOnClickListener {
            episodesInfoClickCallback(item.itemIndex)
        }
        binding.extraEpisodesInfoButton.setOnClickListener {
            episodesInfoClickCallback(item.itemIndex)
        }
        binding.notificationButton.setOnClickListener {
            notificationClickCallback(item.itemIndex)
        }
    }

    private fun bindCommonFields() {
        with(binding) {
            image.isVisible = true
            nameText.isVisible = true
            extraEpisodesInfoButton.backgroundTintList = ColorStateList.valueOf(
                context.getColor(theme_R.color.black)
            )
            availableEpisodesInfoButton.backgroundTintList = ColorStateList.valueOf(
                context.getColor(theme_R.color.black)
            )
            scoreImage.isVisible = true
            scoreText.isVisible = true
            verticalDividerAfterScore.isVisible = true
            verticalDividerAfterStatus.isVisible = true
            notificationButton.backgroundTintList = ColorStateList.valueOf(transparentColor)
            notificationButton.backgroundTintList =
                ColorStateList.valueOf(transparentColor)
            notificationButton.isVisible = true
        }
    }

    private fun bindImageUrl(imageUrl: String) {
        Glide.with(binding.image)
            .load(imageUrl)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.load_image_error_48)
            .into(binding.image)
    }

    private fun bindName(name: String) {
        binding.nameText.text = name
    }

    private fun bindEpisodesInfoType(episodesInfoType: EpisodesInfoTypeUi) {
        with(binding) {
            when (episodesInfoType) {
                EpisodesInfoTypeUi.AVAILABLE -> {
                    extraEpisodesInfoText.isVisible = false
                    availableEpisodesInfoButton.isVisible = false
                    availableEpisodesInfoText.isVisible = true
                    extraEpisodesInfoButton.isVisible = true
                }

                EpisodesInfoTypeUi.FUTURE -> {
                    availableEpisodesInfoText.isVisible = false
                    extraEpisodesInfoButton.isVisible = false
                    extraEpisodesInfoText.isVisible = true
                    availableEpisodesInfoButton.isVisible = true
                }
            }
        }
    }

    private fun bindAvailableEpisodesInfo(availableEpisodesInfo: String) {
        binding.availableEpisodesInfoText.text = availableEpisodesInfo
    }

    private fun bindExtraEpisodesInfo(extraEpisodesInfoInfo: String) {
        binding.extraEpisodesInfoText.text = extraEpisodesInfoInfo
    }

    private fun bindScore(score: String) {
        binding.scoreText.text = score
    }

    private fun bindReleaseStatus(releaseStatus: ReleaseStatusUi) {
        with(binding) {
            when (releaseStatus) {
                ReleaseStatusUi.ONGOING -> {
                    releasedStatusText.text =
                        context.applicationContext.getString(R.string.ongoings)
                    releasedStatusText.setTextColor(context.getColor(theme_R.color.green))
                    releasedStatusText.isVisible = true
                }

                ReleaseStatusUi.ANNOUNCED -> {
                    releasedStatusText.text =
                        context.applicationContext.getString(R.string.announced)
                    releasedStatusText.setTextColor(context.getColor(theme_R.color.purple_200))
                    releasedStatusText.isVisible = true
                }

                ReleaseStatusUi.RELEASED -> {
                    releasedStatusText.text =
                        context.applicationContext.getString(R.string.released)
                    releasedStatusText.setTextColor(context.getColor(theme_R.color.pink))
                    releasedStatusText.isVisible = true
                }

                ReleaseStatusUi.UNKNOWN -> {
                    releasedStatusText.isVisible = false
                }
            }
        }
    }

    private fun bindNotification(notification: NotificationUi) {
        with(binding) {
            when (notification) {
                NotificationUi.ENABLED -> {
                    notificationButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context.applicationContext,
                            R.drawable.ic_notifications_on_40
                        )
                    )
                    notificationButton.rippleColor = context.getColor(theme_R.color.pink)
                    notificationButton.isActivated = true
                    notificationButton.contentDescription = context.resources.getString(
                        R.string.notifications_turn_on
                    )
                }

                NotificationUi.DISABLED -> {
                    notificationButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context.applicationContext,
                            R.drawable.ic_notifications_off_40
                        )
                    )
                    notificationButton.rippleColor = context.getColor(theme_R.color.green)
                    notificationButton.isActivated = false
                    notificationButton.contentDescription = context.resources.getString(
                        R.string.notifications_turn_off
                    )
                }
            }
        }
    }
}