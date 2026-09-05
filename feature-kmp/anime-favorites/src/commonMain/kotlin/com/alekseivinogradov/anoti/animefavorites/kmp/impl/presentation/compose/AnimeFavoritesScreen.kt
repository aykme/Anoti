package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.SuccessResult
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.IMAGE_CORNER_PERCENT
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.LIST_LAST_ITEM_BOTTOM_PADDING_DP
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.PullToRefreshBox
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.AnimeFavoritesUiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list_image_description
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Grey700
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SUBTITLE2_SP
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.horizontalSystemBarsPadding
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.systemBarsTopPadding
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.anime_poster_sample
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.main_character_image
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes

/**
 * The favorites list screen: loading spinner, empty-state panel, or the item list with
 * swipe-to-refresh, depending on [uiModel]'s [AnimeFavoritesUiModel.contentType].
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeFavoritesScreen(
    uiModel: AnimeFavoritesUiModel,
    dateFormatter: DateFormatter,
    dispatch: (AnimeFavoritesMainStore.Intent) -> Unit
) {
    // The only path that flips contentType from LOADING to LOADED, so it must run
    // unconditionally here rather than behind the ContentTypeUi.LOADED branch below.
    LaunchedEffect(uiModel.listItems) {
        if (uiModel.listItems.isNotEmpty()) {
            dispatch(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        }
    }

    Box(Modifier.fillMaxSize().horizontalSystemBarsPadding()) {
        when (uiModel.contentType) {
            ContentTypeUi.LOADING -> LoadingState()
            ContentTypeUi.EMPTY -> EmptyState()
            ContentTypeUi.LOADED -> ListState(
                uiModel = uiModel,
                dateFormatter = dateFormatter,
                dispatch = dispatch
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingSpinner(
            contentDescription = stringResource(BaseRes.string.loading_in_progress),
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp)
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun EmptyState() {
    val topInset = systemBarsTopPadding()
    // Box is required: without it, Row's tight incoming constraints would stretch it to full
    // height despite height(IntrinsicSize.Min).
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(
                    start = 8.dp,
                    // Replaces the default top padding once a real system-bar inset is known,
                    // instead of stacking on top of it.
                    top = if (topInset > 0.dp) topInset else 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                )
        ) {
            Image(
                painter = painterResource(CelebrityRes.drawable.main_character_image),
                contentDescription = stringResource(Res.string.empty_list_image_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(130.dp)
                    .heightIn(min = 130.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(percent = IMAGE_CORNER_PERCENT))
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Grey700, RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = stringResource(Res.string.empty_list),
                    color = White,
                    fontSize = SUBTITLE2_SP,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ListState(
    uiModel: AnimeFavoritesUiModel,
    dateFormatter: DateFormatter,
    dispatch: (AnimeFavoritesMainStore.Intent) -> Unit
) {
    PullToRefreshBox(onRefresh = { dispatch(AnimeFavoritesMainStore.Intent.UpdateSection) }) {
        LazyColumn(
            modifier = Modifier
                .testTag("anime_favorites_rv")
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = systemBarsTopPadding(),
                bottom = LIST_LAST_ITEM_BOTTOM_PADDING_DP.dp
            )
        ) {
            items(uiModel.listItems, key = { it.id }) { item ->
                AnimeFavoritesItem(
                    item = item,
                    dateFormatter = dateFormatter,
                    onItemClick = { dispatch(AnimeFavoritesMainStore.Intent.ItemClick(item.id)) },
                    onInfoTypeClick = {
                        dispatch(AnimeFavoritesMainStore.Intent.InfoTypeClick(item.id))
                    },
                    onNotificationClick = {
                        dispatch(AnimeFavoritesMainStore.Intent.NotificationClick(item.id))
                    },
                    onEpisodesViewedMinusClick = {
                        dispatch(AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick(item.id))
                    },
                    onEpisodesViewedPlusClick = {
                        dispatch(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(item.id))
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

private object AnimeFavoritesScreenPreviewDateFormatter : DateFormatter {
    override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
}

private val previewListItem = ListItemUi(
    id = 1,
    imageUrl = null,
    score = "8.42",
    infoType = InfoTypeUi.MAIN,
    name = "Attack on Titan: Final. Part 2",
    availableEpisodesInfo = "Episodes: 5 / 12",
    releaseStatus = ReleaseStatusUi.ONGOING,
    notification = NotificationUi.ENABLED,
    extraEpisodesInfo = null,
    episodesViewed = "5",
    isNewEpisode = true
)

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun AnimeFavoritesScreenLoadingPreview() {
    AnotiTheme {
        AnimeFavoritesScreen(
            uiModel = AnimeFavoritesUiModel(contentType = ContentTypeUi.LOADING),
            dateFormatter = AnimeFavoritesScreenPreviewDateFormatter,
            dispatch = {}
        )
    }
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun AnimeFavoritesScreenEmptyPreview() {
    AnotiTheme {
        AnimeFavoritesScreen(
            uiModel = AnimeFavoritesUiModel(contentType = ContentTypeUi.EMPTY),
            dateFormatter = AnimeFavoritesScreenPreviewDateFormatter,
            dispatch = {}
        )
    }
}

// Compose Multiplatform's Coil integration renders whatever this provides in place of a real
// network fetch whenever LocalInspectionMode is true (i.e. inside @Preview) — letting every
// AsyncImage reached transitively through AnimeFavoritesItem show a real local image. Success
// (not Loading) is required so a SubcomposeAsyncImage renders this painter instead of its own
// loading slot; the wrapped ColorImage is never actually drawn, only the painter is.
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
@Preview
@Composable
private fun AnimeFavoritesScreenLoadedPreview() {
    AnotiTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides
                rememberPreviewPosterHandler(painterResource(CelebrityRes.drawable.anime_poster_sample))
        ) {
            AnimeFavoritesScreen(
                uiModel = AnimeFavoritesUiModel(
                    contentType = ContentTypeUi.LOADED,
                    listItems = persistentListOf(
                        previewListItem,
                        previewListItem.copy(
                            id = 2,
                            name = "Attack on Titan: Final. Part 1",
                            isNewEpisode = false
                        )
                    )
                ),
                dateFormatter = AnimeFavoritesScreenPreviewDateFormatter,
                dispatch = {}
            )
        }
    }
}
