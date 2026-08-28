// All functions here render pieces of a single favorites item; splitting them across files
// wouldn't make sense.
@file:Suppress("TooManyFunctions")

package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.SubcomposeAsyncImage
import coil3.request.SuccessResult
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
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ACCENT_TEXT_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Black
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.BlackTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.FAB_ELEVATION_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Green
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Grey700
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.IMAGE_CORNER_PERCENT
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ITEM_ICON_ALPHA
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.NOTIFICATION_FAB_SIZE_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.NOTIFICATION_ICON_SIZE_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.POSTER_OVERLAY_ALPHA
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Purple200
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SPACING_UNIT_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SUBTITLE1_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Silver
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.repeatingClickable
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.amiko_bold
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.anime_poster_sample
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
    val strokeColor = if (item.isNewEpisode) Silver else Grey700
    val amikoBold = FontFamily(CmpFont(CelebrityRes.font.amiko_bold, FontWeight.Bold))

    val posterContent: @Composable () -> Unit = {
        PosterColumn(
            imageUrl = item.imageUrl,
            score = item.score,
            infoType = item.infoType,
            isNewEpisode = item.isNewEpisode,
            amikoBold = amikoBold,
            onInfoTypeClick = onInfoTypeClick
        )
    }
    val infoContent: @Composable () -> Unit = {
        MainInfoPanel(
            item = item,
            dateFormatter = dateFormatter,
            strokeColor = strokeColor,
            onNotificationClick = onNotificationClick,
            onEpisodesViewedMinusClick = onEpisodesViewedMinusClick,
            onEpisodesViewedPlusClick = onEpisodesViewedPlusClick
        )
    }
    // The InfoMeasure slot below composes this same content a second time solely to learn its
    // natural height before the real Info slot is measured; clearAndSetSemantics keeps that
    // never-placed copy (and its interactive children, e.g. the notification button) out of the
    // semantics tree so accessibility services and UI tests only ever see one live item.
    val infoMeasureContent: @Composable () -> Unit = {
        Box(Modifier.clearAndSetSemantics {}) {
            infoContent()
        }
    }

    // Row(Modifier.height(IntrinsicSize.Min)) can't be used here: it queries every child's
    // intrinsic height, and PosterColumn contains a SubcomposeAsyncImage (SubcomposeLayout),
    // which throws on intrinsic measurement. SubcomposeLayout lets us learn the info panel's
    // real (non-intrinsic) height first, then measure the poster to match it.
    SubcomposeLayout(
        modifier = Modifier
            .testTag("anime_favorites_item")
            .fillMaxWidth()
            .combinedClickable(onClick = onItemClick, onLongClick = onInfoTypeClick)
            .padding(8.dp)
    ) { constraints ->
        val minHeightPx = ITEM_MIN_HEIGHT_DP.dp.roundToPx()
        val posterWidthPx = POSTER_WIDTH_DP.dp.roundToPx()
        val spacerPx = SPACING_UNIT_DP.roundToPx()
        val infoWidthPx = (constraints.maxWidth - posterWidthPx - spacerPx).coerceAtLeast(0)
        val infoWidthConstraints = Constraints(minWidth = infoWidthPx, maxWidth = infoWidthPx)

        val infoNaturalHeight = subcompose(AnimeFavoritesItemSlot.InfoMeasure, infoMeasureContent)
            .maxOf { it.measure(infoWidthConstraints).height }
        val rowHeight = maxOf(minHeightPx, infoNaturalHeight)

        val posterPlaceables = subcompose(AnimeFavoritesItemSlot.Poster, posterContent)
            .map { it.measure(Constraints.fixed(posterWidthPx, rowHeight)) }
        val infoPlaceables = subcompose(AnimeFavoritesItemSlot.Info, infoContent)
            .map {
                it.measure(
                    infoWidthConstraints.copy(minHeight = rowHeight, maxHeight = rowHeight)
                )
            }

        layout(constraints.maxWidth, rowHeight) {
            posterPlaceables.forEach { it.placeRelative(0, 0) }
            infoPlaceables.forEach { it.placeRelative(posterWidthPx + spacerPx, 0) }
        }
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
            .clip(RoundedCornerShape(percent = IMAGE_CORNER_PERCENT))
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
        color = Silver,
        fontFamily = amikoBold,
        fontWeight = FontWeight.Bold,
        fontSize = ACCENT_TEXT_SP,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            shadow = Shadow(
                color = Black,
                offset = Offset(x = NEW_EPISODE_SHADOW_OFFSET, y = NEW_EPISODE_SHADOW_OFFSET),
                blurRadius = NEW_EPISODE_SHADOW_RADIUS
            )
        ),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(Black.copy(alpha = POSTER_OVERLAY_ALPHA), RoundedCornerShape(4.dp))
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
            .background(Black.copy(alpha = POSTER_OVERLAY_ALPHA), RoundedCornerShape(4.dp))
    ) {
        Icon(
            painter = cmpPainterResource(CelebrityRes.drawable.ic_score_42),
            contentDescription = stringResource(BaseRes.string.score_image_description),
            tint = Cinnabar500,
            modifier = Modifier
                .size(32.dp)
                .alpha(ITEM_ICON_ALPHA)
        )
        Text(
            text = score,
            color = White,
            fontSize = SUBTITLE1_SP,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Centers the button in the space left after the score, rather than pinning it to
        // either side.
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            InfoTypeButton(infoType = infoType, onClick = onInfoTypeClick)
        }
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
    // IconButton's default 48dp minimumInteractiveComponentSize inflates the reported size past
    // whatever the size() modifier below requests, silently growing this 42x34dp button to a
    // 48dp square; disabled here so it keeps its declared size.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        IconButton(
            onClick = onClick,
            // Sits on top of this row's semi-transparent overlay.
            modifier = Modifier
                .size(width = 42.dp, height = 34.dp)
                .background(Black)
                .padding(4.dp)
        ) {
            val infoIcon = if (infoType == InfoTypeUi.MAIN) {
                Res.drawable.ic_details_on_24
            } else {
                Res.drawable.ic_details_off_24
            }
            Icon(
                painter = cmpPainterResource(infoIcon),
                contentDescription = infoTypeDescription,
                tint = Cinnabar500,
                modifier = Modifier.fillMaxSize()
            )
        }
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
                .background(Grey700, RoundedCornerShape(10.dp))
                // 2dp (above) + 6dp = 8dp total padding from the stroke's outer edge.
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
            color = White,
            fontSize = SUBTITLE1_SP,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${stringResource(BaseRes.string.episodes)}: ${item.availableEpisodesInfo}",
            color = White,
            fontSize = SUBTITLE1_SP,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = releaseStatusText(item.releaseStatus),
                color = releaseStatusColor(item.releaseStatus),
                fontSize = SUBTITLE1_SP,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // Keeps an 8dp gap before the FAB even when the text is long enough to fill the
                // row, matching the original's fixed-margin constraint.
                modifier = Modifier.weight(1f).padding(end = 8.dp)
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
            color = White,
            fontSize = SUBTITLE1_SP,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "${stringResource(Res.string.episodes_viewed)}:",
            color = White,
            fontSize = SUBTITLE1_SP,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(3.dp))
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
            tint = Cinnabar500,
            modifier = Modifier
                .size(34.dp)
                .background(Black)
                .indication(minusInteractionSource, ripple())
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
            color = White,
            fontSize = SUBTITLE1_SP,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        val plusInteractionSource = remember { MutableInteractionSource() }
        Icon(
            painter = cmpPainterResource(Res.drawable.ic_arrow_right_32),
            contentDescription = stringResource(Res.string.episodes_viewed_plus_description),
            tint = Cinnabar500,
            modifier = Modifier
                .size(34.dp)
                .background(Black)
                .indication(plusInteractionSource, ripple())
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
    val isEnabled = notification == NotificationUi.ENABLED
    val icon = if (isEnabled) {
        CelebrityRes.drawable.ic_notifications_on_40
    } else {
        CelebrityRes.drawable.ic_notifications_off_40
    }
    val backgroundColor = if (isEnabled) Green else Cinnabar500
    // Ripple color contrasts with the current background, matching the FAB's original styling.
    val rippleColor = if (isEnabled) Cinnabar500 else Green
    val description = stringResource(
        if (isEnabled) {
            BaseRes.string.notifications_turn_off_description
        } else {
            BaseRes.string.notifications_turn_on_description
        }
    )
    CompositionLocalProvider(LocalContentColor provides rippleColor) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .testTag("notification_button")
                .size(NOTIFICATION_FAB_SIZE_DP)
                .alpha(ITEM_ICON_ALPHA)
                .shadow(FAB_ELEVATION_DP, CircleShape)
                .background(backgroundColor, shape = CircleShape)
        ) {
            Icon(
                painter = cmpPainterResource(icon),
                contentDescription = description,
                tint = BlackTransparent,
                modifier = Modifier.size(NOTIFICATION_ICON_SIZE_DP)
            )
        }
    }
}

// ListItemUi.extraEpisodesInfo (see StateToUiModelMapper.kt) is just nextEpisodeAt/airedOn/
// releasedOn, unformatted — needs a release-status-dependent prefix and real date formatting
// before display.
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
        ReleaseStatusUi.ONGOING -> Green
        ReleaseStatusUi.ANNOUNCED -> Purple200
        ReleaseStatusUi.RELEASED -> Cinnabar500
        ReleaseStatusUi.UNKNOWN -> White
    }

private const val NEW_EPISODE_SHADOW_RADIUS = 16f
private const val NEW_EPISODE_SHADOW_OFFSET = 4f
private const val ITEM_MIN_HEIGHT_DP = 146
private const val POSTER_WIDTH_DP = 130

private enum class AnimeFavoritesItemSlot { Poster, Info, InfoMeasure }

private object AnimeFavoritesItemPreviewDateFormatter : DateFormatter {
    override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
}

private val previewItem = ListItemUi(
    id = 1,
    imageUrl = null,
    score = "8.90",
    infoType = InfoTypeUi.MAIN,
    name = "Attack on Titan: Final. Part 2",
    availableEpisodesInfo = "2 / ?",
    releaseStatus = ReleaseStatusUi.ONGOING,
    notification = NotificationUi.ENABLED,
    extraEpisodesInfo = null,
    episodesViewed = "5",
    isNewEpisode = true
)

// Compose Multiplatform's Coil integration renders whatever this provides in place of a real
// network fetch whenever LocalInspectionMode is true (i.e. inside @Preview) — letting every
// AsyncImage/SubcomposeAsyncImage below it, including ones reached transitively through a
// screen-level preview, show a real local image instead of nothing. Success (not Loading) is
// required so a SubcomposeAsyncImage renders this painter instead of its own loading slot; the
// wrapped ColorImage is never actually drawn, only the painter is.
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun rememberPreviewPosterHandler(painter: Painter): AsyncImagePreviewHandler =
    remember(painter) {
        AsyncImagePreviewHandler { _, request ->
            AsyncImagePainter.State.Success(painter, SuccessResult(ColorImage(), request))
        }
    }

// widthDp/heightDp cap the preview's rendering viewport; without both, the unset dimension
// defaults to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
// Sized to this item's own minimum height plus the outer 8dp padding on each side, so the
// preview's background matches just this item, not a whole screen.
private const val PREVIEW_WIDTH_DP = 360
private const val PREVIEW_HEIGHT_DP = ITEM_MIN_HEIGHT_DP + 16

@OptIn(ExperimentalCoilApi::class)
@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = PREVIEW_WIDTH_DP, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun AnimeFavoritesItemMainInfoPreview() {
    AnotiTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides
                rememberPreviewPosterHandler(cmpPainterResource(CelebrityRes.drawable.anime_poster_sample))
        ) {
            AnimeFavoritesItem(
                item = previewItem,
                dateFormatter = AnimeFavoritesItemPreviewDateFormatter,
                onItemClick = {},
                onInfoTypeClick = {},
                onNotificationClick = {},
                onEpisodesViewedMinusClick = {},
                onEpisodesViewedPlusClick = {}
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = PREVIEW_WIDTH_DP, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun AnimeFavoritesItemExtraInfoPreview() {
    AnotiTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides
                rememberPreviewPosterHandler(cmpPainterResource(CelebrityRes.drawable.anime_poster_sample))
        ) {
            AnimeFavoritesItem(
                item = previewItem.copy(
                    infoType = InfoTypeUi.EXTRA,
                    releaseStatus = ReleaseStatusUi.RELEASED,
                    notification = NotificationUi.DISABLED,
                    extraEpisodesInfo = "2026-01-01",
                    episodesViewed = "10",
                    isNewEpisode = false
                ),
                dateFormatter = AnimeFavoritesItemPreviewDateFormatter,
                onItemClick = {},
                onInfoTypeClick = {},
                onNotificationClick = {},
                onEpisodesViewedMinusClick = {},
                onEpisodesViewedPlusClick = {}
            )
        }
    }
}
