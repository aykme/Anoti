// This row reproduces a ~20-view XML layout; splitting each long Composable body into its own
// small function (required to satisfy detekt's LongMethod rule) pushes the file over
// TooManyFunctions' default per-file threshold.
@file:Suppress("TooManyFunctions")

package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
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
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.episodes_viewed
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.episodes_viewed_minus_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.episodes_viewed_plus_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.extra_info_off_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.extra_info_on_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.ic_arrow_left_32
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.ic_arrow_right_32
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.ic_details_off_24
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.ic_details_on_24
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.new_episode
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.repeatingClickable
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.amiko_bold
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_notifications_off_40
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_notifications_on_40
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_score_42
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.load_image_error_48
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import org.jetbrains.compose.resources.stringResource
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes
import org.jetbrains.compose.resources.Font as CmpFont
import org.jetbrains.compose.resources.painterResource as cmpPainterResource

// Colors ported 1:1 from core-kmp/celebrity's colors.xml — not arbitrary magic numbers.
@Suppress("MagicNumber")
private val ItemBlack = Color(0xFF000000)

@Suppress("MagicNumber")
private val ItemWhite = Color(0xFFFFFFFF)

@Suppress("MagicNumber")
private val ItemCinnabarRed = Color(0xFFE84B3D)

@Suppress("MagicNumber")
private val ItemGreen = Color(0xFF81F343)

@Suppress("MagicNumber")
private val ItemDarkGray = Color(0xFF222222)

@Suppress("MagicNumber")
private val ItemSilver = Color(0xFFC0C0C0)

@Suppress("MagicNumber")
private val ItemPurple = Color(0xFFBB86FC)

@Suppress("MagicNumber")
private val ItemBlackTransparent = Color(0xD5000000)

// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming", "LongParameterList")
@Composable
fun AnimeFavoritesItem(
    item: ListItemUi,
    dateFormatter: DateFormatter,
    onItemClick: () -> Unit,
    onInfoTypeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onEpisodesViewedMinusClick: () -> Unit,
    onEpisodesViewedPlusClick: () -> Unit
) {
    val strokeColor = if (item.isNewEpisode) ItemSilver else ItemDarkGray
    val amikoBold = FontFamily(CmpFont(CelebrityRes.font.amiko_bold, FontWeight.Bold))

    Row(
        modifier = Modifier
            .testTag("anime_favorites_item")
            .fillMaxWidth()
            .defaultMinSize(minHeight = 146.dp)
            .height(IntrinsicSize.Min)
            .combinedClickable(onClick = onItemClick, onLongClick = onInfoTypeClick)
            .padding(8.dp)
    ) {
        PosterColumn(
            imageUrl = item.imageUrl,
            score = item.score,
            infoType = item.infoType,
            isNewEpisode = item.isNewEpisode,
            amikoBold = amikoBold,
            onInfoTypeClick = onInfoTypeClick
        )

        Spacer(Modifier.width(8.dp))

        MainInfoPanel(
            item = item,
            dateFormatter = dateFormatter,
            strokeColor = strokeColor,
            onNotificationClick = onNotificationClick,
            onEpisodesViewedMinusClick = onEpisodesViewedMinusClick,
            onEpisodesViewedPlusClick = onEpisodesViewedPlusClick
        )
    }
}

@Suppress("FunctionNaming", "LongParameterList")
@Composable
private fun PosterColumn(
    imageUrl: String?,
    score: String,
    infoType: InfoTypeUi,
    isNewEpisode: Boolean,
    amikoBold: FontFamily,
    onInfoTypeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(percent = ROUNDED_IMAGE_CORNER_PERCENT))
    ) {
        PosterImage(imageUrl)
        if (isNewEpisode) {
            NewEpisodeBadge(amikoBold)
        }
        ScoreInfoBar(score = score, infoType = infoType, onInfoTypeClick = onInfoTypeClick)
    }
}

@Suppress("FunctionNaming")
@Composable
private fun PosterImage(imageUrl: String?) {
    // SubcomposeAsyncImage, not AsyncImage: the loading slot is LoadingSpinner, a genuine
    // animated @Composable (rotation state), not a static Painter — AsyncImage's
    // placeholder/error parameters only accept Painter, which can't host that animation.
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        loading = { LoadingSpinner(modifier = Modifier.fillMaxSize()) },
        error = {
            Image(
                painter = cmpPainterResource(CelebrityRes.drawable.load_image_error_48),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Suppress("FunctionNaming")
@Composable
private fun BoxScope.NewEpisodeBadge(amikoBold: FontFamily) {
    Text(
        text = stringResource(Res.string.new_episode),
        color = ItemSilver,
        fontFamily = amikoBold,
        fontWeight = FontWeight.Bold,
        fontSize = NEW_EPISODE_BADGE_SP.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        // Matches AccentTextAppearance's drop shadow (shadowColor black, radius 16, dx/dy 4).
        style = TextStyle(
            shadow = Shadow(
                color = ItemBlack,
                offset = Offset(x = NEW_EPISODE_SHADOW_OFFSET, y = NEW_EPISODE_SHADOW_OFFSET),
                blurRadius = NEW_EPISODE_SHADOW_RADIUS
            )
        ),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(ItemBlack.copy(alpha = OVERLAY_ALPHA), RoundedCornerShape(4.dp))
            .padding(vertical = 2.dp)
    )
}

@Suppress("FunctionNaming", "LongParameterList")
@Composable
private fun BoxScope.ScoreInfoBar(
    score: String,
    infoType: InfoTypeUi,
    onInfoTypeClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(50.dp)
            .background(ItemBlack.copy(alpha = OVERLAY_ALPHA), RoundedCornerShape(4.dp))
    ) {
        Icon(
            painter = cmpPainterResource(CelebrityRes.drawable.ic_score_42),
            contentDescription = stringResource(BaseRes.string.score_image_description),
            tint = ItemCinnabarRed,
            modifier = Modifier
                .size(32.dp)
                .alpha(SCORE_ICON_ALPHA)
        )
        Text(
            text = score,
            color = ItemWhite,
            fontSize = SUBTITLE1_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        InfoTypeButton(infoType = infoType, onClick = onInfoTypeClick)
    }
}

@Suppress("FunctionNaming")
@Composable
private fun InfoTypeButton(infoType: InfoTypeUi, onClick: () -> Unit) {
    val infoTypeDescription = if (infoType == InfoTypeUi.MAIN) {
        stringResource(Res.string.extra_info_on_description)
    } else {
        stringResource(Res.string.extra_info_off_description)
    }
    IconButton(
        onClick = onClick,
        // The original ImageButton paints an opaque black square behind its icon, on top of
        // this row's semi-transparent overlay.
        modifier = Modifier
            .size(width = 42.dp, height = 34.dp)
            .background(ItemBlack)
    ) {
        val infoIcon = if (infoType == InfoTypeUi.MAIN) {
            Res.drawable.ic_details_on_24
        } else {
            Res.drawable.ic_details_off_24
        }
        Icon(
            painter = cmpPainterResource(infoIcon),
            contentDescription = infoTypeDescription,
            tint = ItemCinnabarRed
        )
    }
}

@Suppress("FunctionNaming", "LongParameterList")
@Composable
private fun MainInfoPanel(
    item: ListItemUi,
    dateFormatter: DateFormatter,
    strokeColor: Color,
    onNotificationClick: () -> Unit,
    onEpisodesViewedMinusClick: () -> Unit,
    onEpisodesViewedPlusClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(strokeColor, RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .background(ItemDarkGray, RoundedCornerShape(10.dp))
                // 2dp (above) + 6dp = 8dp total from the stroke's outer edge, matching the
                // original's name_text/etc. layout_margin of 8dp from main_info_stroke.
                .padding(6.dp)
        ) {
            if (item.infoType == InfoTypeUi.MAIN) {
                MainInfoContent(item = item, onNotificationClick = onNotificationClick)
            } else {
                ExtraInfoContent(
                    item = item,
                    dateFormatter = dateFormatter,
                    onEpisodesViewedMinusClick = onEpisodesViewedMinusClick,
                    onEpisodesViewedPlusClick = onEpisodesViewedPlusClick
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun MainInfoContent(item: ListItemUi, onNotificationClick: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = item.name,
            color = ItemWhite,
            fontSize = SUBTITLE1_SP.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${stringResource(BaseRes.string.episodes)}: ${item.availableEpisodesInfo}",
            color = ItemWhite,
            fontSize = SUBTITLE1_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = releaseStatusText(item.releaseStatus),
                color = releaseStatusColor(item.releaseStatus),
                fontSize = SUBTITLE1_SP.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            NotificationButton(notification = item.notification, onClick = onNotificationClick)
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ExtraInfoContent(
    item: ListItemUi,
    dateFormatter: DateFormatter,
    onEpisodesViewedMinusClick: () -> Unit,
    onEpisodesViewedPlusClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = formatExtraEpisodesInfo(
                extraEpisodesInfo = item.extraEpisodesInfo,
                releaseStatus = item.releaseStatus,
                dateFormatter = dateFormatter
            ),
            color = ItemWhite,
            fontSize = SUBTITLE1_SP.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "${stringResource(Res.string.episodes_viewed)}:",
            color = ItemWhite,
            fontSize = SUBTITLE1_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
        EpisodesViewedRow(
            episodesViewed = item.episodesViewed,
            onMinusClick = onEpisodesViewedMinusClick,
            onPlusClick = onEpisodesViewedPlusClick
        )
    }
}

@Suppress("FunctionNaming", "LongParameterList")
@Composable
private fun EpisodesViewedRow(
    episodesViewed: String,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val minusInteractionSource = remember { MutableInteractionSource() }
        Icon(
            painter = cmpPainterResource(Res.drawable.ic_arrow_left_32),
            contentDescription = stringResource(Res.string.episodes_viewed_minus_description),
            tint = ItemCinnabarRed,
            // The original ImageButton paints an opaque black square behind its icon.
            modifier = Modifier
                .size(34.dp)
                .background(ItemBlack)
                .repeatingClickable(
                    interactionSource = minusInteractionSource,
                    initialDelayMillis = REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS,
                    repeatDelayMillis = REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS,
                    onClick = onMinusClick
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = episodesViewed,
            color = ItemWhite,
            fontSize = SUBTITLE1_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        val plusInteractionSource = remember { MutableInteractionSource() }
        Icon(
            painter = cmpPainterResource(Res.drawable.ic_arrow_right_32),
            contentDescription = stringResource(Res.string.episodes_viewed_plus_description),
            tint = ItemCinnabarRed,
            // The original ImageButton paints an opaque black square behind its icon.
            modifier = Modifier
                .size(34.dp)
                .background(ItemBlack)
                .repeatingClickable(
                    interactionSource = plusInteractionSource,
                    initialDelayMillis = REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS,
                    repeatDelayMillis = REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS,
                    onClick = onPlusClick
                )
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun NotificationButton(notification: NotificationUi, onClick: () -> Unit) {
    val (icon, backgroundColor, description) = if (notification == NotificationUi.ENABLED) {
        Triple(
            CelebrityRes.drawable.ic_notifications_on_40,
            ItemGreen,
            stringResource(BaseRes.string.notifications_turn_off_description)
        )
    } else {
        Triple(
            CelebrityRes.drawable.ic_notifications_off_40,
            ItemCinnabarRed,
            stringResource(BaseRes.string.notifications_turn_on_description)
        )
    }
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .testTag("notification_button")
            .size(NOTIFICATION_BUTTON_SIZE.dp)
            .alpha(NOTIFICATION_BUTTON_ALPHA)
            .background(backgroundColor, shape = CircleShape)
    ) {
        Icon(
            painter = cmpPainterResource(icon),
            contentDescription = description,
            tint = ItemBlackTransparent,
            modifier = Modifier.size(NOTIFICATION_ICON_SIZE.dp)
        )
    }
}

// Ported 1:1 from AnimeFavoritesViewHolder.getExtraEpisodesInfo() — the raw date string in
// ListItemUi.extraEpisodesInfo (see StateToUiModelMapper.kt: it's just nextEpisodeAt/airedOn/
// releasedOn, unformatted) needs release-status-dependent prefix text and real date formatting
// before display; this logic used to live in the View layer and still does, just in Compose now.
@Composable
private fun formatExtraEpisodesInfo(
    extraEpisodesInfo: String?,
    releaseStatus: ReleaseStatusUi,
    dateFormatter: DateFormatter
): String {
    val noDataString = stringResource(CelebrityRes.string.no_data)
    val formattedDate = if (extraEpisodesInfo?.isNotEmpty() == true) {
        dateFormatter.getFormattedDate(inputText = extraEpisodesInfo, fallbackText = noDataString)
    } else {
        noDataString
    }
    return when (releaseStatus) {
        ReleaseStatusUi.ONGOING ->
            "${stringResource(BaseRes.string.next_episode)}:\n$formattedDate"

        ReleaseStatusUi.ANNOUNCED -> {
            val inaccurateSuffix = if (extraEpisodesInfo?.isNotEmpty() == true) {
                " (${stringResource(BaseRes.string.inaccurate)})"
            } else {
                ""
            }
            "${stringResource(BaseRes.string.beginning_of_the_show)}:\n$formattedDate$inaccurateSuffix"
        }

        ReleaseStatusUi.RELEASED ->
            "${stringResource(BaseRes.string.show_is_finished)}:\n$formattedDate"

        ReleaseStatusUi.UNKNOWN -> formattedDate
    }
}

@Composable
private fun releaseStatusText(status: ReleaseStatusUi): String =
    when (status) {
        ReleaseStatusUi.ONGOING -> stringResource(BaseRes.string.ongoing)
        ReleaseStatusUi.ANNOUNCED -> stringResource(BaseRes.string.announced)
        ReleaseStatusUi.RELEASED -> stringResource(BaseRes.string.released)
        ReleaseStatusUi.UNKNOWN -> ""
    }

private fun releaseStatusColor(status: ReleaseStatusUi): Color =
    when (status) {
        ReleaseStatusUi.ONGOING -> ItemGreen
        ReleaseStatusUi.ANNOUNCED -> ItemPurple
        ReleaseStatusUi.RELEASED -> ItemCinnabarRed
        ReleaseStatusUi.UNKNOWN -> ItemWhite
    }

private const val ROUNDED_IMAGE_CORNER_PERCENT = 8
private const val OVERLAY_ALPHA = 0.5f
private const val NEW_EPISODE_BADGE_SP = 15
private const val SUBTITLE1_SP = 16
private const val NOTIFICATION_BUTTON_SIZE = 56
private const val NOTIFICATION_ICON_SIZE = 40
private const val SCORE_ICON_ALPHA = 0.8f
private const val NOTIFICATION_BUTTON_ALPHA = 0.8f
private const val NEW_EPISODE_SHADOW_RADIUS = 16f
private const val NEW_EPISODE_SHADOW_OFFSET = 4f
