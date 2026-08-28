// All functions here render pieces of a single list item; splitting them across files wouldn't
// make sense.
@file:Suppress("TooManyFunctions")

package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
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
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.available_episodes_info_discription
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.extra_episodes_info_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_info_28
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_info_outline_28
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.poster_image_description
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.NOTIFICATION_FAB_SIZE_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.NOTIFICATION_ICON_SIZE_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SUBTITLE1_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Black
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.BlackTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Green
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Purple200
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.WhiteTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.anime_poster_sample
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_notifications_off_40
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_notifications_on_40
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_score_42
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes

/**
 * A single item in the anime list: poster, name, episode/release info, score, and a
 * notification toggle.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming", "LongParameterList")
@Composable
fun AnimeListItem(
    item: ListItemUi,
    dateFormatter: DateFormatter,
    onEpisodesInfoClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(8.dp)
            .testTag("anime_list_item")
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = stringResource(Res.string.poster_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(POSTER_HEIGHT_DP.dp)
                .clip(RoundedCornerShape(percent = POSTER_CORNER_PERCENT))
        )

        NameEpisodesAndBottomRow(
            item = item,
            dateFormatter = dateFormatter,
            onEpisodesInfoClick = onEpisodesInfoClick,
            onNotificationClick = onNotificationClick
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun BoxScope.NameEpisodesAndBottomRow(
    item: ListItemUi,
    dateFormatter: DateFormatter,
    onEpisodesInfoClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(INFO_BACKGROUND_ALPHA)
                .background(Black, RoundedCornerShape(4.dp))
        )
        Column {
            Column(modifier = Modifier.padding(start = 11.dp, end = 11.dp, top = 8.dp)) {
                Text(
                    text = item.name,
                    color = White,
                    fontSize = HEADLINE6_SP.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                EpisodesInfoRow(
                    item = item,
                    dateFormatter = dateFormatter,
                    onEpisodesInfoClick = onEpisodesInfoClick
                )
            }
            BottomRow(item = item, onNotificationClick = onNotificationClick)
        }
    }
}

// Long because it renders two mutually exclusive, fully self-contained branches (each with its
// own Text and FloatingActionButton), not because of nested logic.
@Suppress("FunctionNaming", "LongMethod")
@Composable
private fun EpisodesInfoRow(
    item: ListItemUi,
    dateFormatter: DateFormatter,
    onEpisodesInfoClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // Applied on the row, not the text: the FAB centers against the text's own bounds, and
        // this bottom margin must sit outside those bounds to keep that centering exact.
        modifier = if (item.episodesInfoType == EpisodesInfoTypeUi.AVAILABLE) {
            Modifier.padding(bottom = 8.dp)
        } else {
            Modifier.padding(bottom = 10.5.dp)
        }
    ) {
        if (item.episodesInfoType == EpisodesInfoTypeUi.AVAILABLE) {
            Text(
                text = availableEpisodesInfoText(item),
                color = White,
                fontSize = HEADLINE6_SP.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).padding(vertical = 8.dp)
            )
            Spacer(Modifier.width(3.dp))
            CompositionLocalProvider(LocalRippleConfiguration provides RippleConfiguration(color = Black)) {
                FloatingActionButton(
                    onClick = onEpisodesInfoClick,
                    containerColor = Black,
                    contentColor = Cinnabar500,
                    shape = CircleShape,
                    modifier = Modifier.padding(top = 1.dp).size(FAB_SIZE_DP.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info_outline_28),
                        contentDescription = stringResource(
                            Res.string.extra_episodes_info_description
                        ),
                        modifier = Modifier.size(FAB_ICON_SIZE_DP.dp)
                    )
                }
            }
        } else {
            Text(
                text = formatExtraEpisodesInfo(item = item, dateFormatter = dateFormatter),
                color = White,
                fontSize = SUBTITLE1_SP,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(3.dp))
            CompositionLocalProvider(LocalRippleConfiguration provides RippleConfiguration(color = Black)) {
                FloatingActionButton(
                    onClick = onEpisodesInfoClick,
                    containerColor = Black,
                    contentColor = Cinnabar500,
                    shape = CircleShape,
                    modifier = Modifier.padding(top = 3.dp).size(FAB_SIZE_DP.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info_28),
                        contentDescription = stringResource(
                            Res.string.available_episodes_info_discription
                        ),
                        modifier = Modifier.size(FAB_ICON_SIZE_DP.dp)
                    )
                }
            }
        }
    }
}

// Long because it lays out four sibling bindings (score, divider, release status, notification)
// each with its own exact spacing/color, not because of nested logic.
@Suppress("FunctionNaming", "LongMethod")
@Composable
private fun BottomRow(item: ListItemUi, onNotificationClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(CelebrityRes.drawable.ic_score_42),
            contentDescription = stringResource(BaseRes.string.score_image_description),
            tint = Cinnabar500,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(42.dp)
                .alpha(SCORE_NOTIFICATION_ALPHA)
        )
        Text(
            text = item.score,
            color = White,
            fontSize = HEADLINE6_SP.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 11.dp)
        )
        if (item.releaseStatus != ReleaseStatusUi.UNKNOWN) {
            // The divider/status/divider trio floats between the score and the notification
            // button as a chain, each gap sharing the row's leftover space equally — not one
            // single gap absorbing all of it.
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 5.dp, end = 4.dp)
                    .size(width = 3.dp, height = 10.dp)
                    .background(WhiteTransparent)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = releaseStatusText(item.releaseStatus),
                color = releaseStatusColor(item.releaseStatus),
                fontSize = HEADLINE6_SP.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 5.dp, end = 4.dp)
                    .size(width = 3.dp, height = 10.dp)
                    .background(WhiteTransparent)
            )
        }
        Spacer(Modifier.weight(1f))
        // Ripple color is intentionally the state it would switch TO on tap, not a match to
        // the current background.
        val (notificationIcon, notificationBackground, notificationRipple) =
            if (item.notification == NotificationUi.ENABLED) {
                Triple(CelebrityRes.drawable.ic_notifications_on_40, Green, Cinnabar500)
            } else {
                Triple(CelebrityRes.drawable.ic_notifications_off_40, Cinnabar500, Green)
            }
        val notificationDescription = if (item.notification == NotificationUi.ENABLED) {
            stringResource(BaseRes.string.notifications_turn_off_description)
        } else {
            stringResource(BaseRes.string.notifications_turn_on_description)
        }
        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(color = notificationRipple)
        ) {
            FloatingActionButton(
                onClick = onNotificationClick,
                containerColor = notificationBackground,
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(NOTIFICATION_FAB_SIZE_DP)
                    .alpha(SCORE_NOTIFICATION_ALPHA)
                    .testTag("notification_button")
            ) {
                Icon(
                    painter = painterResource(notificationIcon),
                    contentDescription = notificationDescription,
                    tint = BlackTransparent,
                    modifier = Modifier.size(NOTIFICATION_ICON_SIZE_DP)
                )
            }
        }
    }
}

@Composable
private fun availableEpisodesInfoText(item: ListItemUi): String {
    val isReleased = item.releaseStatus == ReleaseStatusUi.RELEASED
    val episodesAired = if (!isReleased) {
        item.episodesAired ?: 0
    } else {
        item.episodesTotal ?: item.episodesAired ?: 0
    }
    val episodesTotal = item.episodesTotal ?: 0
    val episodesTotalText = if (episodesTotal > 0) episodesTotal.toString() else "?"
    return "${stringResource(BaseRes.string.episodes)}: $episodesAired / $episodesTotalText"
}

@Composable
private fun formatExtraEpisodesInfo(item: ListItemUi, dateFormatter: DateFormatter): String {
    val noDataString = stringResource(CelebrityRes.string.no_data)
    val rawDate = when (item.releaseStatus) {
        ReleaseStatusUi.ONGOING -> item.nextEpisodeAt
        ReleaseStatusUi.ANNOUNCED -> item.airedOn
        ReleaseStatusUi.RELEASED -> item.releasedOn
        ReleaseStatusUi.UNKNOWN -> null
    }
    val formattedDate = if (!rawDate.isNullOrEmpty()) {
        dateFormatter.getFormattedDate(inputText = rawDate, fallbackText = noDataString)
    } else {
        noDataString
    }
    return when (item.releaseStatus) {
        ReleaseStatusUi.ONGOING -> "${stringResource(BaseRes.string.next_episode)}:\n$formattedDate"
        ReleaseStatusUi.ANNOUNCED -> {
            val inaccurateSuffix = if (!rawDate.isNullOrEmpty()) {
                " (${stringResource(BaseRes.string.inaccurate)})"
            } else {
                ""
            }
            "${stringResource(BaseRes.string.beginning_of_the_show)}:\n$formattedDate$inaccurateSuffix"
        }
        ReleaseStatusUi.RELEASED -> "${stringResource(BaseRes.string.show_is_finished)}:\n$formattedDate"
        ReleaseStatusUi.UNKNOWN -> formattedDate
    }
}

@Composable
private fun releaseStatusText(releaseStatus: ReleaseStatusUi): String = when (releaseStatus) {
    ReleaseStatusUi.ONGOING -> stringResource(BaseRes.string.ongoing)
    ReleaseStatusUi.ANNOUNCED -> stringResource(BaseRes.string.announced)
    ReleaseStatusUi.RELEASED -> stringResource(BaseRes.string.released)
    ReleaseStatusUi.UNKNOWN -> ""
}

private fun releaseStatusColor(releaseStatus: ReleaseStatusUi): Color = when (releaseStatus) {
    ReleaseStatusUi.ONGOING -> Green
    ReleaseStatusUi.ANNOUNCED -> Purple200
    ReleaseStatusUi.RELEASED -> Cinnabar500
    ReleaseStatusUi.UNKNOWN -> White
}

private const val POSTER_HEIGHT_DP = 350
private const val POSTER_CORNER_PERCENT = 3
private const val HEADLINE6_SP = 20
private const val FAB_SIZE_DP = 35
private const val FAB_ICON_SIZE_DP = 28
private const val INFO_BACKGROUND_ALPHA = 0.5f
private const val SCORE_NOTIFICATION_ALPHA = 0.8f

// widthDp/heightDp cap the preview's rendering viewport; without both, the unset dimension
// defaults to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
// Sized to the poster's fixed height plus the outer 8dp padding on each side, so the preview's
// background matches just this item, not a whole screen.
private const val PREVIEW_WIDTH_DP = 360
private const val PREVIEW_HEIGHT_DP = POSTER_HEIGHT_DP + 16

private object AnimeListItemPreviewDateFormatter : DateFormatter {
    override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
}

private val previewItem = ListItemUi(
    id = 1,
    name = "Attack on Titan: Final. Part 2",
    imageUrl = null,
    episodesInfoType = EpisodesInfoTypeUi.AVAILABLE,
    episodesAired = 2,
    episodesTotal = null,
    nextEpisodeAt = null,
    airedOn = null,
    releasedOn = null,
    score = "8.90",
    releaseStatus = ReleaseStatusUi.ONGOING,
    notification = NotificationUi.ENABLED
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

@OptIn(ExperimentalCoilApi::class)
@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = PREVIEW_WIDTH_DP, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun AnimeListItemAvailablePreview() {
    AnotiTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides
                rememberPreviewPosterHandler(painterResource(CelebrityRes.drawable.anime_poster_sample))
        ) {
            AnimeListItem(
                item = previewItem,
                dateFormatter = AnimeListItemPreviewDateFormatter,
                onEpisodesInfoClick = {},
                onNotificationClick = {}
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview(widthDp = PREVIEW_WIDTH_DP, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun AnimeListItemExtraPreview() {
    AnotiTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides
                rememberPreviewPosterHandler(painterResource(CelebrityRes.drawable.anime_poster_sample))
        ) {
            AnimeListItem(
                item = previewItem.copy(
                    episodesInfoType = EpisodesInfoTypeUi.EXTRA,
                    airedOn = "20 feb. 2022",
                    releaseStatus = ReleaseStatusUi.ANNOUNCED,
                    notification = NotificationUi.DISABLED
                ),
                dateFormatter = AnimeListItemPreviewDateFormatter,
                onEpisodesInfoClick = {},
                onNotificationClick = {}
            )
        }
    }
}
