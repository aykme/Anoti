package com.alekseivinogradov.anime_favorites.impl.presentation.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.InfoTypeUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.NotificationUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ReleaseStatusUi
import com.alekseivinogradov.anime_favorites_platform.R
import com.alekseivinogradov.anime_favorites_platform.databinding.ItemAnimeFavoritesBinding
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.repeat_listener.RepeatListener
import com.bumptech.glide.Glide
import com.alekseivinogradov.atom.R as atom_R
import com.alekseivinogradov.theme.R as theme_R

internal class AnimeFavoritesViewHolder(
    private val binding: ItemAnimeFavoritesBinding,
    private val itemClickViewHolderCallback: (Int) -> Unit,
    private val infoTypeClickViewHolderCallback: (Int) -> Unit,
    private val notificationClickViewHolderCallback: (Int) -> Unit,
    private val episodesViewedMinusClickViewHolderCallback: (Int) -> Unit,
    private val episodesViewedPlusClickViewHolderCallback: (Int) -> Unit,
    private val dateFormatter: DateFormatter
) :
    RecyclerView.ViewHolder(binding.root) {

    private val context: Context
        get() = binding.root.context

    private val disableColor: Int
        get() = context.getColor(theme_R.color.pink)

    private val enableColor: Int
        get() = context.getColor(theme_R.color.green)

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

    internal fun bindWithPayload(payload: AnimeFavoritesPayload) {
        when (payload) {
            is AnimeFavoritesPayload.ImageUrlChange -> {
                bindImageUrl(payload.imageUrl)
            }

            is AnimeFavoritesPayload.ScoreChange -> {
                bindScore(payload.score)
            }

            is AnimeFavoritesPayload.InfoTypeChange -> {
                bindInfoType(payload.infoType)
            }

            is AnimeFavoritesPayload.NameChange -> {
                bindName(payload.name)
            }

            is AnimeFavoritesPayload.AvailableEpisodesInfoChange -> {
                bindAvailableEpisodesInfo(payload.availableEpisodesInfo)
            }

            is AnimeFavoritesPayload.ReleaseStatusChange -> {
                bindReleaseStatus(payload.releaseStatus)
            }

            is AnimeFavoritesPayload.NotificationChange -> {
                bindNotification(payload.notification)
            }

            is AnimeFavoritesPayload.ExtraEpisodesInfoChange -> {
                bindExtraEpisodesInfo(
                    extraEpisodesInfo = payload.extraEpisodesInfo,
                    releaseStatus = payload.releaseStatus
                )
            }

            is AnimeFavoritesPayload.EpisodesViewedChange -> {
                bindEpisodesViewed(payload.episodesViewed)
            }

            is AnimeFavoritesPayload.NewEpisodeStatusChange -> {
                bindNewEpisodeStatus(payload.isNewEpisode)
            }
        }
    }

    internal fun bind(item: ListItemUi) {
        bindCommonFields()
        bindImageUrl(item.imageUrl)
        bindScore(item.score)
        bindInfoType(item.infoType)
        bindName(item.name)
        bindAvailableEpisodesInfo(item.availableEpisodesInfo)
        bindReleaseStatus(item.releaseStatus)
        bindNotification(item.notification)
        bindExtraEpisodesInfo(
            extraEpisodesInfo = item.extraEpisodesInfo,
            releaseStatus = item.releaseStatus
        )
        bindEpisodesViewed(item.episodesViewed)
        bindNewEpisodeStatus(item.isNewEpisode)
    }

    private fun bindCommonFields() {
        with(binding) {
            image.isVisible = true
            newEpisodeText.text = context.getString(R.string.new_episode)
            imageInfoBackground.isVisible = true
            scoreImage.isVisible = true
            scoreText.isVisible = true
            infoTypeButton.isVisible = true
            mainInfoStroke.isVisible = true
            mainInfoBackground.isVisible = true
            releaseStatusBarrier.isInvisible = true
            val episodesViewedText = "${context.getString(R.string.episodes_viewed)}:"
            episodesViewedTitle.text = episodesViewedText
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListeners() {
        with(binding) {
            root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                itemClickViewHolderCallback(bindingAdapterPosition)
            }
            root.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    infoTypeClickViewHolderCallback(bindingAdapterPosition)
                    true
                } else false
            }
            infoTypeButton.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                infoTypeClickViewHolderCallback(bindingAdapterPosition)
            }
            notificationButton.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                notificationClickViewHolderCallback(bindingAdapterPosition)
            }
            episodesViewedMinusButton.setOnTouchListener(
                RepeatListener(
                    initialInterval = com.alekseivinogradov.celebrity.api.domain.REPEAT_LISTENER_INITIAL_INTERVAL,
                    repeatInterval = com.alekseivinogradov.celebrity.api.domain.REPEAT_LISTENER_REPEAT_INTERVAL
                ) {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@RepeatListener
                    episodesViewedMinusClickViewHolderCallback(bindingAdapterPosition)
                }
            )
            episodesViewedPlusButton.setOnTouchListener(
                RepeatListener(
                    initialInterval = com.alekseivinogradov.celebrity.api.domain.REPEAT_LISTENER_INITIAL_INTERVAL,
                    repeatInterval = com.alekseivinogradov.celebrity.api.domain.REPEAT_LISTENER_REPEAT_INTERVAL
                ) {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@RepeatListener
                    episodesViewedPlusClickViewHolderCallback(bindingAdapterPosition)
                }
            )
        }
    }

    private fun bindImageUrl(imageUrl: String?) {
        imageUrl?.let {
            Glide.with(binding.image)
                .load(it)
                .placeholder(atom_R.drawable.loading_animation)
                .error(atom_R.drawable.load_image_error_48)
                .into(binding.image)
        }
    }

    private fun bindName(name: String) {
        binding.nameText.text = name
    }

    private fun bindInfoType(InfoType: InfoTypeUi) {
        with(binding) {
            when (InfoType) {
                InfoTypeUi.MAIN -> {
                    infoTypeButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_details_on_24
                        )
                    )
                    infoTypeButton.contentDescription = context.getString(
                        R.string.extra_info_on_description
                    )
                    extraEpisodesInfoText.isVisible = false
                    episodesViewedTitle.isVisible = false
                    episodesViewedMinusButton.isVisible = false
                    episodesViewedNumber.isVisible = false
                    episodesViewedPlusButton.isVisible = false
                    nameText.isVisible = true
                    availableEpisodesInfoText.isVisible = true
                    releaseStatusText.isVisible = true
                    notificationButton.isVisible = true
                }

                InfoTypeUi.EXTRA -> {
                    infoTypeButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_details_off_24
                        )
                    )
                    infoTypeButton.contentDescription = context.getString(
                        R.string.extra_info_off_description
                    )
                    nameText.isVisible = false
                    availableEpisodesInfoText.isVisible = false
                    releaseStatusText.isVisible = false
                    notificationButton.isVisible = false
                    extraEpisodesInfoText.isVisible = true
                    episodesViewedTitle.isVisible = true
                    episodesViewedMinusButton.isVisible = true
                    episodesViewedNumber.isVisible = true
                    episodesViewedPlusButton.isVisible = true
                }
            }
        }
    }

    private fun bindAvailableEpisodesInfo(availableEpisodesInfo: String) {
        val availableEpisodesInfoText = "$episodesString: $availableEpisodesInfo"
        binding.availableEpisodesInfoText.text = availableEpisodesInfoText
    }

    private fun bindExtraEpisodesInfo(
        extraEpisodesInfo: String?,
        releaseStatus: ReleaseStatusUi
    ) {
        binding.extraEpisodesInfoText.text = getExtraEpisodesInfo(
            extraEpisodesInfo = extraEpisodesInfo,
            releaseStatus = releaseStatus
        )
    }

    private fun getExtraEpisodesInfo(
        extraEpisodesInfo: String?,
        releaseStatus: ReleaseStatusUi
    ): String {
        val extraEpisodesInfoFormatted =
            if (extraEpisodesInfo?.isNotEmpty() == true) {
                dateFormatter.getFormattedDate(
                    inputText = extraEpisodesInfo,
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
                    if (extraEpisodesInfo?.isNotEmpty() == true) {
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

    @SuppressLint("SetTextI18n")
    private fun bindReleaseStatus(releaseStatus: ReleaseStatusUi) {
        with(binding) {
            when (releaseStatus) {
                ReleaseStatusUi.ONGOING -> {
                    releaseStatusText.text =
                        context.getString(R.string.ongoings)
                    releaseStatusText.setTextColor(context.getColor(theme_R.color.green))
                }

                ReleaseStatusUi.ANNOUNCED -> {
                    releaseStatusText.text =
                        context.getString(R.string.announced)
                    releaseStatusText.setTextColor(context.getColor(theme_R.color.purple_200))
                }

                ReleaseStatusUi.RELEASED -> {
                    releaseStatusText.text =
                        context.getString(R.string.released)
                    releaseStatusText.setTextColor(context.getColor(theme_R.color.pink))
                }

                ReleaseStatusUi.UNKNOWN -> {
                    releaseStatusText.text = ""
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
                            atom_R.drawable.ic_notifications_on_40
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
                            atom_R.drawable.ic_notifications_off_40
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

    private fun bindEpisodesViewed(episodesViewed: String) {
        binding.episodesViewedNumber.text = episodesViewed
    }

    private fun bindNewEpisodeStatus(isNewEpisode: Boolean) {
        with(binding) {
            if (isNewEpisode) {
                newEpisodeBackground.isVisible = true
                newEpisodeText.isVisible = true
                mainInfoStroke.backgroundTintList = ColorStateList.valueOf(
                    context.getColor(theme_R.color.silver)
                )
            } else {
                newEpisodeText.isVisible = false
                newEpisodeBackground.isVisible = false
                mainInfoStroke.backgroundTintList = ColorStateList.valueOf(
                    context.getColor(theme_R.color.grey_700)
                )
            }
        }
    }
}
