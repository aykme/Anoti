package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.Barrier
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anoti.animefavorites.kmp.R
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.platform.impl.presentation.repeatlistener.RepeatListener
import com.alekseivinogradov.anoti.res.R as res_R
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

internal class AnimeFavoritesViewHolder(
    itemView: View,
    private val itemClickViewHolderCallback: (Int) -> Unit,
    private val infoTypeClickViewHolderCallback: (Int) -> Unit,
    private val notificationClickViewHolderCallback: (Int) -> Unit,
    private val episodesViewedMinusClickViewHolderCallback: (Int) -> Unit,
    private val episodesViewedPlusClickViewHolderCallback: (Int) -> Unit,
    private val dateFormatter: DateFormatter
) : RecyclerView.ViewHolder(itemView) {

    private val posterImage: ShapeableImageView = itemView.findViewById(R.id.poster_image)
    private val newEpisodeBackground: View = itemView.findViewById(R.id.new_episode_background)
    private val newEpisodeText: MaterialTextView = itemView.findViewById(R.id.new_episode_text)
    private val imageInfoBackground: View = itemView.findViewById(R.id.image_info_background)
    private val scoreImage: AppCompatImageView = itemView.findViewById(R.id.score_image)
    private val scoreText: MaterialTextView = itemView.findViewById(R.id.score_text)
    private val infoTypeButton: AppCompatImageButton = itemView.findViewById(R.id.info_type_button)
    private val mainInfoStroke: View = itemView.findViewById(R.id.main_info_stroke)
    private val mainInfoBackground: View = itemView.findViewById(R.id.main_info_background)
    private val nameText: MaterialTextView = itemView.findViewById(R.id.name_text)
    private val availableEpisodesInfoText: MaterialTextView =
        itemView.findViewById(R.id.available_episodes_info_text)
    private val releaseStatusBarrier: Barrier = itemView.findViewById(R.id.release_status_barrier)
    private val releaseStatusText: MaterialTextView =
        itemView.findViewById(R.id.release_status_text)
    private val notificationButton: FloatingActionButton =
        itemView.findViewById(R.id.notification_button)
    private val extraEpisodesInfoText: MaterialTextView =
        itemView.findViewById(R.id.extra_episodes_info_text)
    private val episodesViewedTitle: MaterialTextView =
        itemView.findViewById(R.id.episodes_viewed_title)
    private val episodesViewedMinusButton: AppCompatImageButton =
        itemView.findViewById(R.id.episodes_viewed_minus_button)
    private val episodesViewedNumber: MaterialTextView =
        itemView.findViewById(R.id.episodes_viewed_number)
    private val episodesViewedPlusButton: AppCompatImageButton =
        itemView.findViewById(R.id.episodes_viewed_plus_button)

    private val context: Context
        get() = itemView.context

    private val disableColor: Int
        get() = context.getColor(res_R.color.cinnabar_500)

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
        posterImage.isVisible = true
        newEpisodeText.text = context.getString(R.string.new_episode)
        imageInfoBackground.isVisible = true
        scoreImage.contentDescription = context.getString(R.string.score_image_description)
        scoreImage.isVisible = true
        scoreText.isVisible = true
        infoTypeButton.isVisible = true
        mainInfoStroke.isVisible = true
        mainInfoBackground.isVisible = true
        releaseStatusBarrier.isInvisible = true
        val episodesViewedText = "${context.getString(R.string.episodes_viewed)}:"
        episodesViewedTitle.text = episodesViewedText
        episodesViewedMinusButton.contentDescription = context
            .getString(R.string.episodes_viewed_minus_description)
        episodesViewedPlusButton.contentDescription = context
            .getString(R.string.episodes_viewed_plus_description)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListeners() {
        itemView.setOnClickListener {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            itemClickViewHolderCallback(bindingAdapterPosition)
        }
        itemView.setOnLongClickListener {
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
                initialInterval = REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS,
                repeatInterval = REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS
            ) {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@RepeatListener
                episodesViewedMinusClickViewHolderCallback(bindingAdapterPosition)
            }
        )
        episodesViewedPlusButton.setOnTouchListener(
            RepeatListener(
                initialInterval = REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS,
                repeatInterval = REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS
            ) {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@RepeatListener
                episodesViewedPlusClickViewHolderCallback(bindingAdapterPosition)
            }
        )
    }

    private fun bindImageUrl(imageUrl: String?) {
        Glide.with(posterImage)
            .load(imageUrl)
            .placeholder(res_R.drawable.loading_animation)
            .error(res_R.drawable.load_image_error_48)
            .into(posterImage)
    }

    private fun bindName(name: String) {
        nameText.text = name
    }

    private fun bindInfoType(infoType: InfoTypeUi) {
        when (infoType) {
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

    private fun bindAvailableEpisodesInfo(availableEpisodesInfo: String) {
        val availableEpisodesInfoTextValue = "$episodesString: $availableEpisodesInfo"
        availableEpisodesInfoText.text = availableEpisodesInfoTextValue
    }

    private fun bindExtraEpisodesInfo(
        extraEpisodesInfo: String?,
        releaseStatus: ReleaseStatusUi
    ) {
        extraEpisodesInfoText.text = getExtraEpisodesInfo(
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
        scoreText.text = score
    }

    @SuppressLint("SetTextI18n")
    private fun bindReleaseStatus(releaseStatus: ReleaseStatusUi) {
        when (releaseStatus) {
            ReleaseStatusUi.ONGOING -> {
                releaseStatusText.text =
                    context.getString(R.string.ongoing)
                releaseStatusText.setTextColor(context.getColor(res_R.color.green))
            }

            ReleaseStatusUi.ANNOUNCED -> {
                releaseStatusText.text =
                    context.getString(R.string.announced)
                releaseStatusText.setTextColor(context.getColor(res_R.color.purple_200))
            }

            ReleaseStatusUi.RELEASED -> {
                releaseStatusText.text =
                    context.getString(R.string.released)
                releaseStatusText.setTextColor(context.getColor(res_R.color.cinnabar_500))
            }

            ReleaseStatusUi.UNKNOWN -> {
                releaseStatusText.text = ""
            }
        }
    }

    private fun bindNotification(notification: NotificationUi) {
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

    private fun bindEpisodesViewed(episodesViewed: String) {
        episodesViewedNumber.text = episodesViewed
    }

    private fun bindNewEpisodeStatus(isNewEpisode: Boolean) {
        if (isNewEpisode) {
            newEpisodeBackground.isVisible = true
            newEpisodeText.isVisible = true
            mainInfoStroke.backgroundTintList = ColorStateList.valueOf(
                context.getColor(res_R.color.silver)
            )
        } else {
            newEpisodeText.isVisible = false
            newEpisodeBackground.isVisible = false
            mainInfoStroke.backgroundTintList = ColorStateList.valueOf(
                context.getColor(res_R.color.grey_700)
            )
        }
    }
}
