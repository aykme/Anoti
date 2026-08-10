package com.alekseivinogradov.anoti.animelist.android.impl.presentation.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anoti.animelist.kmp.R
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.res.R as res_R
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

internal class AnimeListViewHolder(
    itemView: View,
    private val episodesInfoClickViewHolderCallback: (Int) -> Unit,
    private val notificationClickViewHolderCallback: (Int) -> Unit,
    private val dateFormatter: DateFormatter
) :
    RecyclerView.ViewHolder(itemView) {

    private val context: Context
        get() = itemView.context

    private val posterImage: ShapeableImageView =
        itemView.findViewById(R.id.poster_image)
    private val infoBackground: View =
        itemView.findViewById(R.id.info_background)
    private val nameText: MaterialTextView =
        itemView.findViewById(R.id.name_text)
    private val availableEpisodesInfoText: MaterialTextView =
        itemView.findViewById(R.id.available_episodes_info_text)
    private val extraEpisodesInfoButton: FloatingActionButton =
        itemView.findViewById(R.id.extra_episodes_info_button)
    private val extraEpisodesInfoText: MaterialTextView =
        itemView.findViewById(R.id.extra_episodes_info_text)
    private val availableEpisodesInfoButton: FloatingActionButton =
        itemView.findViewById(R.id.available_episodes_info_button)
    private val notificationButtonBarrier: View =
        itemView.findViewById(R.id.notification_button_barrier)
    private val scoreImage: View =
        itemView.findViewById(R.id.score_image)
    private val scoreText: MaterialTextView =
        itemView.findViewById(R.id.score_text)
    private val verticalDividerAfterScore: View =
        itemView.findViewById(R.id.vertical_divider_after_score)
    private val verticalDividerAfterStatus: View =
        itemView.findViewById(R.id.vertical_divider_after_status)
    private val releaseStatusText: MaterialTextView =
        itemView.findViewById(R.id.release_status_text)
    private val notificationButton: FloatingActionButton =
        itemView.findViewById(R.id.notification_button)

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
        itemView.isVisible = true
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

    private fun setClickListeners() {
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

    private fun bindEpisodesInfoType(episodesInfoType: EpisodesInfoTypeUi) {
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

    private fun bindAvailableEpisodesInfo(
        episodesAired: Int?,
        episodesTotal: Int?,
        releaseStatus: ReleaseStatusUi
    ) {
        availableEpisodesInfoText.text = getAvailableEpisodesInfo(
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
        val episodesTotalString = if (episodesTotalNotNull > 0) {
            episodesTotalNotNull.toString()
        } else "?"

        return "$episodesString: $episodesAiredString / $episodesTotalString"
    }

    private fun bindExtraEpisodesInfo(
        nextEpisodeAt: String?,
        airedOn: String?,
        releasedOn: String?,
        releaseStatus: ReleaseStatusUi
    ) {
        extraEpisodesInfoText.text = getExtraEpisodesInfo(
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
        scoreText.text = score
    }

    private fun bindReleaseStatus(releaseStatus: ReleaseStatusUi) {
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
                releaseStatusText.setTextColor(context.getColor(res_R.color.cinnabar_500))
                releaseStatusText.isVisible = true
            }

            ReleaseStatusUi.UNKNOWN -> {
                releaseStatusText.isVisible = false
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
}
