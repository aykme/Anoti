package com.alekseivinogradov.anoti.animelist.android.impl.presentation.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as baseRes
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.announced
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.beginning_of_the_show
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.episodes
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.inaccurate
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.next_episode
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_off_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_on_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.ongoing
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.released
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.score_image_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.show_is_finished
import com.alekseivinogradov.anoti.animelist.kmp.R
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.available_episodes_info_discription
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.extra_episodes_info_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.poster_image_description
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

internal class AnimeListViewHolder(
    itemView: View,
    private val episodesInfoClickViewHolderCallback: (Int) -> Unit,
    private val notificationClickViewHolderCallback: (Int) -> Unit,
    private val dateFormatter: DateFormatter,
    coroutineContextProvider: CoroutineContextProvider
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

    private val posterImageDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.poster_image_description)
        }
    private val extraEpisodesInfoDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.extra_episodes_info_description)
        }
    private val availableEpisodesInfoDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.available_episodes_info_discription)
        }
    private val scoreImageDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.score_image_description)
        }
    private val episodesString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.episodes)
        }
    private val nextEpisodeString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.next_episode)
        }
    private val beginningOfTheShowString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.beginning_of_the_show)
        }
    private val showIsFinishedString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.show_is_finished)
        }
    private val noDataString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(celebrityRes.string.no_data)
        }
    private val inaccurateString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.inaccurate)
        }
    private val ongoingString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.ongoing)
        }
    private val announcedString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.announced)
        }
    private val releasedString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.released)
        }
    private val notificationsTurnOffDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.notifications_turn_off_description)
        }
    private val notificationsTurnOnDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.notifications_turn_on_description)
        }

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
        posterImage.contentDescription = posterImageDescriptionString
        posterImage.isVisible = true
        infoBackground.isVisible = true
        nameText.isVisible = true
        extraEpisodesInfoButton.backgroundTintList = ColorStateList.valueOf(
            context.getColor(res_R.color.black)
        )
        extraEpisodesInfoButton.contentDescription = extraEpisodesInfoDescriptionString
        availableEpisodesInfoButton.backgroundTintList = ColorStateList.valueOf(
            context.getColor(res_R.color.black)
        )
        availableEpisodesInfoButton.contentDescription = availableEpisodesInfoDescriptionString
        notificationButtonBarrier.isInvisible = true
        scoreImage.contentDescription = scoreImageDescriptionString
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
                releaseStatusText.text = ongoingString
                releaseStatusText.setTextColor(context.getColor(res_R.color.green))
                releaseStatusText.isVisible = true
            }

            ReleaseStatusUi.ANNOUNCED -> {
                releaseStatusText.text = announcedString
                releaseStatusText.setTextColor(context.getColor(res_R.color.purple_200))
                releaseStatusText.isVisible = true
            }

            ReleaseStatusUi.RELEASED -> {
                releaseStatusText.text = releasedString
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
                notificationButton.contentDescription = notificationsTurnOffDescriptionString
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
                notificationButton.contentDescription = notificationsTurnOnDescriptionString
            }
        }
    }
}
