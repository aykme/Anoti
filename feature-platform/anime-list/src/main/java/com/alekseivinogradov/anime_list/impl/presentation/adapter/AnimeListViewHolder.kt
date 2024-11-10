package com.alekseivinogradov.anime_list.impl.presentation.adapter

import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anime_list.api.presentation.model.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.ReleaseStatusUi
import com.alekseivinogradov.anime_list_platform.R
import com.alekseivinogradov.anime_list_platform.databinding.ItemAnimeListBinding
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatter
import com.bumptech.glide.Glide
import com.alekseivinogradov.res.R as res_R

internal class AnimeListViewHolder(
    private val binding: ItemAnimeListBinding,
    private val episodesInfoClickViewHolderCallback: (Int) -> Unit,
    private val notificationClickViewHolderCallback: (Int) -> Unit,
    private val dateFormatter: DateFormatter
) :
    RecyclerView.ViewHolder(binding.root) {

    private val context: Context
        get() = binding.root.context

    private val disableColor: Int
        get() = context.getColor(res_R.color.pink)

    private val enableColor: Int
        get() = context.getColor(res_R.color.green)

    private val episodesString: String
        get() = context.getString(R.string.episodes)

    private val nextEpisodeString: String
        get() = context.getString(R.string.next_episode)

    private val beginningOfTheShowString: String
        get() = context.getString(R.string.beginning_of_the_show)

    private val showIsFinishedString: String
        get() = context.getString(R.string.show_is_finished)

    private val noDataString: String
        get() = context.getString(R.string.no_data)

    private val inaccurateString: String
        get() = context.getString(R.string.inaccurate)

    init {
        setClickListeners()
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
                bindAvailableEpisodesInfo(
                    episodesAired = payload.episodesAired,
                    episodesTotal = payload.episodesTotal,
                    releaseStatus = payload.releaseStatus
                )
            }

            is AnimeListPayload.ExtraEpisodesInfoChange -> {
                bindExtraEpisodesInfo(
                    nextEpisodeAt = payload.nextEpisodeAt,
                    airedOn = payload.airedOn,
                    releasedOn = payload.releasedOn,
                    releaseStatus = payload.releaseStatus
                )
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
        bindAvailableEpisodesInfo(
            episodesAired = item.episodesAired,
            episodesTotal = item.episodesTotal,
            releaseStatus = item.releaseStatus
        )
        bindExtraEpisodesInfo(
            nextEpisodeAt = item.nextEpisodeAt,
            airedOn = item.airedOn,
            releasedOn = item.releasedOn,
            releaseStatus = item.releaseStatus
        )
        bindScore(item.score)
        bindReleaseStatus(item.releaseStatus)
        bindNotification(item.notification)
    }

    private fun bindCommonFields() {
        with(binding) {
            itemAnimeListLayout.isVisible = true
            posterImage.contentDescription = context.getString(R.string.poster_image_description)
            posterImage.isVisible = true
            infoBackground.isVisible = true
            nameText.isVisible = true
            extraEpisodesInfoButton.backgroundTintList = ColorStateList.valueOf(
                context.getColor(res_R.color.black)
            )
            extraEpisodesInfoButton.contentDescription = context
                .getString(R.string.extra_episodes_info_description)
            availableEpisodesInfoButton.backgroundTintList = ColorStateList.valueOf(
                context.getColor(res_R.color.black)
            )
            availableEpisodesInfoButton.contentDescription = context
                .getString(R.string.available_episodes_info_discription)
            notificationButtonBarrier.isInvisible = true
            scoreImage.contentDescription = context.getString(R.string.score_image_description)
            scoreImage.isVisible = true
            scoreText.isVisible = true
            verticalDividerAfterScore.isVisible = true
            verticalDividerAfterStatus.isVisible = true
            notificationButton.isVisible = true
        }
    }

    private fun setClickListeners() {
        with(binding) {
            availableEpisodesInfoButton.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                episodesInfoClickViewHolderCallback(bindingAdapterPosition)
            }
            extraEpisodesInfoButton.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                episodesInfoClickViewHolderCallback(bindingAdapterPosition)
            }
            notificationButton.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                notificationClickViewHolderCallback(bindingAdapterPosition)
            }
        }
    }

    private fun bindImageUrl(imageUrl: String?) {
        Glide.with(binding.posterImage)
            .load(imageUrl)
            .placeholder(res_R.drawable.loading_animation)
            .error(res_R.drawable.load_image_error_48)
            .into(binding.posterImage)
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

                EpisodesInfoTypeUi.EXTRA -> {
                    availableEpisodesInfoText.isVisible = false
                    extraEpisodesInfoButton.isVisible = false
                    extraEpisodesInfoText.isVisible = true
                    availableEpisodesInfoButton.isVisible = true
                }
            }
        }
    }

    private fun bindAvailableEpisodesInfo(
        episodesAired: Int?,
        episodesTotal: Int?,
        releaseStatus: ReleaseStatusUi
    ) {
        binding.availableEpisodesInfoText.text = getAvailableEpisodesInfo(
            episodesAired = episodesAired,
            episodesTotal = episodesTotal,
            releaseStatus = releaseStatus
        )
    }

    private fun getAvailableEpisodesInfo(
        episodesAired: Int?,
        episodesTotal: Int?,
        releaseStatus: ReleaseStatusUi
    ): String {
        val isReleased = releaseStatus == ReleaseStatusUi.RELEASED

        val episodesAiredString = if (isReleased.not()) {
            episodesAired ?: 0
        } else {
            episodesTotal ?: episodesAired ?: 0
        }

        val episodesTotalNotNull = episodesTotal ?: 0
        val episotesTotalString = if (episodesTotalNotNull > 0) {
            episodesTotalNotNull.toString()
        } else "?"

        return "$episodesString: $episodesAiredString / $episotesTotalString"
    }

    private fun bindExtraEpisodesInfo(
        nextEpisodeAt: String?,
        airedOn: String?,
        releasedOn: String?,
        releaseStatus: ReleaseStatusUi
    ) {
        binding.extraEpisodesInfoText.text = getExtraEpisodesInfo(
            nextEpisodeAt = nextEpisodeAt,
            airedOn = airedOn,
            releasedOn = releasedOn,
            releaseStatus = releaseStatus
        )
    }

    private fun getExtraEpisodesInfo(
        nextEpisodeAt: String?,
        airedOn: String?,
        releasedOn: String?,
        releaseStatus: ReleaseStatusUi
    ): String {
        val extraEpisodesInfoNotFormatted = when (releaseStatus) {
            ReleaseStatusUi.ONGOING -> nextEpisodeAt
            ReleaseStatusUi.ANNOUNCED -> airedOn
            ReleaseStatusUi.RELEASED -> releasedOn
            ReleaseStatusUi.UNKNOWN -> null
        }
        val extraEpisodesInfoFormatted =
            if (extraEpisodesInfoNotFormatted?.isNotEmpty() == true) {
                dateFormatter.getFormattedDate(
                    inputText = extraEpisodesInfoNotFormatted,
                    fallbackText = noDataString
                )
            } else {
                noDataString
            }
        val extraEpisodesInfoFullString = when (releaseStatus) {
            ReleaseStatusUi.ONGOING -> {
                "$nextEpisodeString:\n$extraEpisodesInfoFormatted"
            }

            ReleaseStatusUi.ANNOUNCED -> {
                val commentAfterDateString =
                    if (extraEpisodesInfoNotFormatted?.isNotEmpty() == true) {
                        " ($inaccurateString)"
                    } else {
                        ""
                    }
                "$beginningOfTheShowString:\n$extraEpisodesInfoFormatted$commentAfterDateString"
            }

            ReleaseStatusUi.RELEASED -> {
                "$showIsFinishedString:\n$extraEpisodesInfoFormatted"
            }

            ReleaseStatusUi.UNKNOWN -> extraEpisodesInfoFormatted
        }

        return extraEpisodesInfoFullString
    }

    private fun bindScore(score: String) {
        binding.scoreText.text = score
    }

    private fun bindReleaseStatus(releaseStatus: ReleaseStatusUi) {
        with(binding) {
            when (releaseStatus) {
                ReleaseStatusUi.ONGOING -> {
                    releaseStatusText.text =
                        context.getString(R.string.ongoing)
                    releaseStatusText.setTextColor(context.getColor(res_R.color.green))
                    releaseStatusText.isVisible = true
                }

                ReleaseStatusUi.ANNOUNCED -> {
                    releaseStatusText.text =
                        context.getString(R.string.announced)
                    releaseStatusText.setTextColor(context.getColor(res_R.color.purple_200))
                    releaseStatusText.isVisible = true
                }

                ReleaseStatusUi.RELEASED -> {
                    releaseStatusText.text =
                        context.getString(R.string.released)
                    releaseStatusText.setTextColor(context.getColor(res_R.color.pink))
                    releaseStatusText.isVisible = true
                }

                ReleaseStatusUi.UNKNOWN -> {
                    releaseStatusText.isVisible = false
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
                            context,
                            res_R.drawable.ic_notifications_on_40
                        )
                    )
                    notificationButton.rippleColor = disableColor
                    notificationButton.backgroundTintList = ColorStateList.valueOf(enableColor)
                    notificationButton.contentDescription = context.resources.getString(
                        R.string.notifications_turn_off_description
                    )
                }

                NotificationUi.DISABLED -> {
                    notificationButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            res_R.drawable.ic_notifications_off_40
                        )
                    )
                    notificationButton.rippleColor = enableColor
                    notificationButton.backgroundTintList = ColorStateList.valueOf(disableColor)
                    notificationButton.contentDescription = context.resources.getString(
                        R.string.notifications_turn_on_description
                    )
                }
            }
        }
    }
}
