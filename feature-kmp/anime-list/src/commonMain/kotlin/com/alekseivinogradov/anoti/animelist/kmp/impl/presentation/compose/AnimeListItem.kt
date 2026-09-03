// All functions here render pieces of a single list item; splitting them across files wouldn't
// make sense.
@file:Suppress("TooManyFunctions")

package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
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
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.FAB_ELEVATION_DP
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ITEM_ICON_ALPHA
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.NOTIFICATION_FAB_SIZE_DP
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.NOTIFICATION_ICON_SIZE_DP
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.POSTER_OVERLAY_ALPHA
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.SECONDARY_FAB_ICON_SIZE_DP
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.SECONDARY_FAB_SIZE_DP
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.announced
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.episodes
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.inaccurate
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_off_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_on_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.ongoing
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.released
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.score_image_description
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.EPISODES_AVAILABLE_MAX_LINES
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.EPISODES_EXTRA_MAX_LINES
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.GAP_COUNT_WITH_STATUS
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.ITEM_PREVIEW_HEIGHT_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.ITEM_PREVIEW_WIDTH_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.POSTER_CORNER_PERCENT
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.POSTER_HEIGHT_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TITLE_MAX_LINES
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.available_episodes_info_discription
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.beginning_of_the_show
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.extra_episodes_info_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_info_28
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.ic_info_outline_28
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.next_episode
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.poster_image_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.show_is_finished
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Black
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.BlackTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Green
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.HEADLINE6_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Purple200
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SUBTITLE1_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.WhiteTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.anime_poster_sample
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_notifications_off_40
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_notifications_on_40
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.ic_score_42
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.load_image_error_48
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
        // SubcomposeAsyncImage, not AsyncImage: the loading slot is LoadingSpinner, a genuine
        // animated @Composable (rotation state), not a static Painter — AsyncImage's
        // placeholder/error parameters only accept Painter, which can't host that animation.
        SubcomposeAsyncImage(
            model = item.imageUrl,
            contentDescription = stringResource(Res.string.poster_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(POSTER_HEIGHT_DP.dp)
                .clip(RoundedCornerShape(percent = POSTER_CORNER_PERCENT)),
            loading = { LoadingSpinner(modifier = Modifier.fillMaxWidth().height(POSTER_HEIGHT_DP.dp)) },
            error = {
                Image(
                    painter = painterResource(CelebrityRes.drawable.load_image_error_48),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(POSTER_HEIGHT_DP.dp)
                )
            }
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
                .alpha(POSTER_OVERLAY_ALPHA)
                .background(Black, RoundedCornerShape(4.dp))
        )
        Column {
            Column(modifier = Modifier.padding(start = 11.dp, end = 11.dp, top = 8.dp)) {
                Text(
                    text = item.name,
                    color = White,
                    fontSize = HEADLINE6_SP,
                    fontWeight = FontWeight.Medium,
                    maxLines = TITLE_MAX_LINES,
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
                fontSize = HEADLINE6_SP,
                fontWeight = FontWeight.Medium,
                maxLines = EPISODES_AVAILABLE_MAX_LINES,
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
                    modifier = Modifier.padding(top = 1.dp).size(SECONDARY_FAB_SIZE_DP)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info_outline_28),
                        contentDescription = stringResource(
                            Res.string.extra_episodes_info_description
                        ),
                        modifier = Modifier.size(SECONDARY_FAB_ICON_SIZE_DP)
                    )
                }
            }
        } else {
            Text(
                text = formatExtraEpisodesInfo(item = item, dateFormatter = dateFormatter),
                color = White,
                fontSize = SUBTITLE1_SP,
                fontWeight = FontWeight.Normal,
                maxLines = EPISODES_EXTRA_MAX_LINES,
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
                    modifier = Modifier.padding(top = 3.dp).size(SECONDARY_FAB_SIZE_DP)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info_28),
                        contentDescription = stringResource(
                            Res.string.available_episodes_info_discription
                        ),
                        modifier = Modifier.size(SECONDARY_FAB_ICON_SIZE_DP)
                    )
                }
            }
        }
    }
}

private enum class BottomRowSlot { ScoreIcon, Score, Divider1, Status, Divider2, Fab }

// A plain Row can't express "give the status text its full natural width for as long as it
// fits, only ellipsize it once it genuinely doesn't". Row's weight() always splits leftover
// space by weight fraction regardless of how much a child actually needs, which shrank the
// status text even when there was plenty of room to spare. This custom Layout measures the
// status text's natural (single-line) width via intrinsics first, and only constrains its real
// measurement if that natural width wouldn't fit. so standard/most scales render exactly like
// a fixed-size Row would, and only extreme font/display scales fall back to ellipsizing it,
// with the notification FAB always kept at its full size.
@Suppress("FunctionNaming", "LongMethod")
@Composable
private fun BottomRow(item: ListItemUi, onNotificationClick: () -> Unit) {
    val hasStatus = item.releaseStatus != ReleaseStatusUi.UNKNOWN
    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            Icon(
                painter = painterResource(CelebrityRes.drawable.ic_score_42),
                contentDescription = stringResource(BaseRes.string.score_image_description),
                tint = Cinnabar500,
                modifier = Modifier
                    .layoutId(BottomRowSlot.ScoreIcon)
                    .padding(start = 8.dp)
                    .size(42.dp)
                    .alpha(ITEM_ICON_ALPHA)
            )
            Text(
                text = item.score,
                color = White,
                fontSize = HEADLINE6_SP,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.layoutId(BottomRowSlot.Score).padding(start = 11.dp)
            )
            if (hasStatus) {
                Divider(Modifier.layoutId(BottomRowSlot.Divider1))
                Text(
                    text = releaseStatusText(item.releaseStatus),
                    color = releaseStatusColor(item.releaseStatus),
                    fontSize = HEADLINE6_SP,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.layoutId(BottomRowSlot.Status)
                )
                Divider(Modifier.layoutId(BottomRowSlot.Divider2))
            }
            NotificationFab(
                item = item,
                onClick = onNotificationClick,
                modifier = Modifier.layoutId(BottomRowSlot.Fab)
            )
        }
    ) { measurables, constraints ->
        measureBottomRow(measurables, constraints, hasStatus)
    }
}

private fun MeasureScope.measureBottomRow(
    measurables: List<Measurable>,
    constraints: Constraints,
    hasStatus: Boolean
): MeasureResult {
    val byId = measurables.associateBy { it.layoutId }
    val loose = Constraints()

    val scoreIcon = byId.getValue(BottomRowSlot.ScoreIcon).measure(loose)
    val score = byId.getValue(BottomRowSlot.Score).measure(loose)
    val divider1 = if (hasStatus) byId.getValue(BottomRowSlot.Divider1).measure(loose) else null
    val divider2 = if (hasStatus) byId.getValue(BottomRowSlot.Divider2).measure(loose) else null
    val fab = byId.getValue(BottomRowSlot.Fab).measure(loose)

    val fixedWidth = scoreIcon.width + score.width + (divider1?.width ?: 0) +
        (divider2?.width ?: 0) + fab.width
    val available = constraints.maxWidth

    // gaps: before divider1, before status, before divider2, before the FAB.
    val gapCount = if (hasStatus) GAP_COUNT_WITH_STATUS else 1
    val status: Placeable?
    val gapWidth: Int
    if (hasStatus) {
        val statusMeasurable = byId.getValue(BottomRowSlot.Status)
        val statusNaturalWidth = statusMeasurable.maxIntrinsicWidth(scoreIcon.height)
        if (fixedWidth + statusNaturalWidth <= available) {
            status = statusMeasurable.measure(loose)
            gapWidth = ((available - fixedWidth - status.width) / gapCount).coerceAtLeast(0)
        } else {
            val remainingForStatus = (available - fixedWidth).coerceAtLeast(0)
            status = statusMeasurable.measure(Constraints(maxWidth = remainingForStatus))
            gapWidth = 0
        }
    } else {
        status = null
        gapWidth = ((available - fixedWidth) / gapCount).coerceAtLeast(0)
    }

    val rowHeight = listOfNotNull(scoreIcon, score, divider1, status, divider2, fab)
        .maxOf { it.height }

    return layout(available, rowHeight) {
        var x = 0
        fun place(placeable: Placeable?) {
            if (placeable == null) return
            placeable.placeRelative(x, (rowHeight - placeable.height) / 2)
            x += placeable.width
        }
        place(scoreIcon)
        place(score)
        if (hasStatus) {
            x += gapWidth
            place(divider1)
            x += gapWidth
            place(status)
            x += gapWidth
            place(divider2)
        }
        x += gapWidth
        place(fab)
    }
}

@Suppress("FunctionNaming")
@Composable
private fun NotificationFab(item: ListItemUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            onClick = onClick,
            containerColor = notificationBackground,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = FAB_ELEVATION_DP),
            modifier = modifier
                .padding(end = 16.dp, bottom = 16.dp)
                .size(NOTIFICATION_FAB_SIZE_DP)
                .alpha(ITEM_ICON_ALPHA)
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

@Suppress("FunctionNaming")
@Composable
private fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 4.dp, top = 5.dp, end = 4.dp)
            .size(width = 3.dp, height = 10.dp)
            .background(WhiteTransparent)
    )
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
    // A regular space only after the label, non-breaking spaces around the count: the only
    // place this can wrap onto a second line is right after "Episodes:".
    return "${stringResource(BaseRes.string.episodes)}: $episodesAired / $episodesTotalText"
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
        ReleaseStatusUi.ONGOING -> "${stringResource(Res.string.next_episode)}:\n$formattedDate"
        ReleaseStatusUi.ANNOUNCED -> {
            val inaccurateSuffix = if (!rawDate.isNullOrEmpty()) {
                " (${stringResource(BaseRes.string.inaccurate)})"
            } else {
                ""
            }
            "${stringResource(Res.string.beginning_of_the_show)}:\n$formattedDate$inaccurateSuffix"
        }
        ReleaseStatusUi.RELEASED -> "${stringResource(Res.string.show_is_finished)}:\n$formattedDate"
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
@Preview(widthDp = ITEM_PREVIEW_WIDTH_DP, heightDp = ITEM_PREVIEW_HEIGHT_DP)
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
@Preview(widthDp = ITEM_PREVIEW_WIDTH_DP, heightDp = ITEM_PREVIEW_HEIGHT_DP)
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
