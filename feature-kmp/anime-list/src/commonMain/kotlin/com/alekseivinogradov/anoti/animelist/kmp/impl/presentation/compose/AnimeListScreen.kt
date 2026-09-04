package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.SuccessResult
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.PAGING_PREFETCH_DISTANCE
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.LIST_LAST_ITEM_BOTTOM_PADDING_DP
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.PullToRefreshBox
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListContentUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.connection_error_48
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.horizontalSystemBarsPadding
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.systemBarsTopPadding
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.anime_poster_sample
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes

/**
 * The anime list screen: top bar plus loading spinner, error state, or the selected section's
 * item list with pull-to-refresh and pagination, depending on [uiModel]'s [UiModel.contentType].
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeListScreen(
    uiModel: UiModel,
    dateFormatter: DateFormatter,
    dispatch: (AnimeListMainStore.Intent) -> Unit
) {
    Box(Modifier.fillMaxSize().horizontalSystemBarsPadding()) {
        when (uiModel.contentType) {
            ContentTypeUi.LOADING -> LoadingState()
            ContentTypeUi.ERROR -> ErrorState(dispatch = dispatch)
            ContentTypeUi.LOADED -> ListState(
                uiModel = uiModel,
                dateFormatter = dateFormatter,
                dispatch = dispatch
            )
        }
        // Drawn last so the top bar sits above the list content.
        AnimeListTopBar(
            selectedSection = uiModel.selectedSection,
            search = uiModel.search,
            onOngoingClick = { dispatch(AnimeListMainStore.Intent.OngoingsSectionClick) },
            onAnnouncedClick = { dispatch(AnimeListMainStore.Intent.AnnouncedSectionClick) },
            onSearchClick = { dispatch(AnimeListMainStore.Intent.SearchSectionClick) },
            onCancelSearchClick = { dispatch(AnimeListMainStore.Intent.CancelSearchClick) },
            onSearchTextChange = { dispatch(AnimeListMainStore.Intent.ChangeSearchText(it)) }
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingSpinner(
            contentDescription = stringResource(BaseRes.string.loading_in_progress),
            modifier = Modifier.fillMaxSize().padding(64.dp)
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ErrorState(dispatch: (AnimeListMainStore.Intent) -> Unit) {
    PullToRefreshBox(onRefresh = { dispatch(AnimeListMainStore.Intent.UpdateSection) }) {
        Image(
            painter = painterResource(Res.drawable.connection_error_48),
            contentDescription = stringResource(CelebrityRes.string.connection_error),
            colorFilter = ColorFilter.tint(SilverTransparent),
            modifier = Modifier.fillMaxSize().padding(64.dp)
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ListState(
    uiModel: UiModel,
    dateFormatter: DateFormatter,
    dispatch: (AnimeListMainStore.Intent) -> Unit
) {
    val listState = rememberLazyListState()

    LoadNextPageEffect(listState = listState, dispatch = dispatch)
    ResetListPositionEffect(uiModel = uiModel, listState = listState, dispatch = dispatch)

    PullToRefreshBox(onRefresh = { dispatch(AnimeListMainStore.Intent.UpdateSection) }) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = systemBarsTopPadding(),
                bottom = LIST_LAST_ITEM_BOTTOM_PADDING_DP.dp
            )
        ) {
            // Keyed on section + id: the same anime can appear both in search results and in a
            // section list (e.g. an ongoing show matching the search query), so scoping the key
            // by section keeps those two appearances from ever being treated as one item moving
            // within the same list.
            items(
                uiModel.listContent.listItems,
                key = { "${uiModel.selectedSection.name}_${it.id}" }
            ) { item ->
                AnimeListItem(
                    item = item,
                    dateFormatter = dateFormatter,
                    onEpisodesInfoClick = {
                        dispatch(AnimeListMainStore.Intent.EpisodesInfoClick(item.id))
                    },
                    onNotificationClick = {
                        dispatch(AnimeListMainStore.Intent.NotificationClick(item.id))
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun LoadNextPageEffect(
    listState: LazyListState,
    dispatch: (AnimeListMainStore.Intent) -> Unit
) {
    // Dispatches once per threshold-crossing: the effect only restarts when the derived boolean
    // itself flips, not on every scroll position update while it stays true.
    val shouldLoadNextPage by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val totalCount = layoutInfo.totalItemsCount
            totalCount > 0 && lastVisible >= totalCount - PAGING_PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(shouldLoadNextPage) {
        if (shouldLoadNextPage) {
            dispatch(AnimeListMainStore.Intent.LoadNextPage)
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ResetListPositionEffect(
    uiModel: UiModel,
    listState: LazyListState,
    dispatch: (AnimeListMainStore.Intent) -> Unit
) {
    // Keyed on the flag itself, not the list, so this dispatch fires exactly once per reset.
    LaunchedEffect(uiModel.listContent.isNeedToResetListPositon) {
        if (uiModel.listContent.isNeedToResetListPositon) {
            listState.scrollToItem(0)
            dispatch(
                AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                    isNeedToResetListPosition = false
                )
            )
        }
    }
}

private object AnimeListScreenPreviewDateFormatter : DateFormatter {
    override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
}

private val previewListItem = ListItemUi(
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

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun AnimeListScreenLoadingPreview() {
    AnotiTheme {
        AnimeListScreen(
            uiModel = UiModel(contentType = ContentTypeUi.LOADING),
            dateFormatter = AnimeListScreenPreviewDateFormatter,
            dispatch = {}
        )
    }
}

@Suppress("FunctionNaming", "UnusedPrivateMember")
@Preview
@Composable
private fun AnimeListScreenErrorPreview() {
    AnotiTheme {
        AnimeListScreen(
            uiModel = UiModel(contentType = ContentTypeUi.ERROR),
            dateFormatter = AnimeListScreenPreviewDateFormatter,
            dispatch = {}
        )
    }
}

// Compose Multiplatform's Coil integration renders whatever this provides in place of a real
// network fetch whenever LocalInspectionMode is true (i.e. inside @Preview) — letting every
// AsyncImage reached transitively through AnimeListItem show a real local image. Success (not
// Loading) is required so a SubcomposeAsyncImage renders this painter instead of its own loading
// slot; the wrapped ColorImage is never actually drawn, only the painter is.
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
private fun AnimeListScreenLoadedPreview() {
    AnotiTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides
                rememberPreviewPosterHandler(painterResource(CelebrityRes.drawable.anime_poster_sample))
        ) {
            AnimeListScreen(
                uiModel = UiModel(
                    contentType = ContentTypeUi.LOADED,
                    listContent = ListContentUi(
                        listItems = listOf(
                            previewListItem,
                            previewListItem.copy(id = 2, name = "Attack on Titan: Final. Part 1")
                        )
                    )
                ),
                dateFormatter = AnimeListScreenPreviewDateFormatter,
                dispatch = {}
            )
        }
    }
}
