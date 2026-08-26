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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.connection_error_48
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.LIST_LAST_ITEM_BOTTOM_PADDING_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.PAGING_PREFETCH_DISTANCE
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.PULL_TO_REFRESH_THRESHOLD
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
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
    topInsetDp: Dp = 0.dp,
    dispatch: (AnimeListMainStore.Intent) -> Unit
) {
    AnimeListTopBar(
        selectedSection = uiModel.selectedSection,
        search = uiModel.search,
        topInsetDp = topInsetDp,
        onOngoingClick = { dispatch(AnimeListMainStore.Intent.OngoingsSectionClick) },
        onAnnouncedClick = { dispatch(AnimeListMainStore.Intent.AnnouncedSectionClick) },
        onSearchClick = { dispatch(AnimeListMainStore.Intent.SearchSectionClick) },
        onCancelSearchClick = { dispatch(AnimeListMainStore.Intent.CancelSearchClick) },
        onSearchTextChange = { dispatch(AnimeListMainStore.Intent.ChangeSearchText(it)) }
    )

    // Top bar and content share one Box, top bar drawn last so it sits above the list.
    Box(Modifier.fillMaxSize()) {
        when (uiModel.contentType) {
            ContentTypeUi.LOADING -> LoadingState()
            ContentTypeUi.ERROR -> ErrorState()
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
            modifier = Modifier.fillMaxSize().padding(64.dp)
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ErrorState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Res.drawable.connection_error_48),
            contentDescription = stringResource(CelebrityRes.string.connection_error),
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
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    LoadNextPageEffect(listState = listState, dispatch = dispatch)
    ResetListPositionEffect(uiModel = uiModel, listState = listState, dispatch = dispatch)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                isRefreshing = false,
                state = pullToRefreshState,
                threshold = PULL_TO_REFRESH_THRESHOLD,
                onRefresh = { dispatch(AnimeListMainStore.Intent.UpdateSection) }
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = LIST_LAST_ITEM_BOTTOM_PADDING_DP.dp)
        ) {
            items(uiModel.listContent.listItems, key = { it.id }) { item ->
                AnimeListItem(
                    item = item,
                    dateFormatter = dateFormatter,
                    onEpisodesInfoClick = {
                        dispatch(AnimeListMainStore.Intent.EpisodesInfoClick(item.id))
                    },
                    onNotificationClick = {
                        dispatch(AnimeListMainStore.Intent.NotificationClick(item.id))
                    }
                )
            }
        }
        // maxDistance is independent of pullToRefresh's threshold above and defaults to 80dp —
        // without setting it, the release point moves but the indicator's travel doesn't.
        PullToRefreshDefaults.Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = false,
            state = pullToRefreshState,
            color = Cinnabar500,
            containerColor = White,
            maxDistance = PULL_TO_REFRESH_THRESHOLD
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun LoadNextPageEffect(
    listState: LazyListState,
    dispatch: (AnimeListMainStore.Intent) -> Unit
) {
    // Keyed on the derived boolean, not the raw scroll position, so this dispatches once per
    // threshold-crossing rather than on every pixel scrolled while already near the end.
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
    // Checking isNeedToResetListPositon before dispatching false keeps this a one-shot reset —
    // dispatching unconditionally would re-fire on every recomposition the effect key allows.
    LaunchedEffect(uiModel.listContent.listItems) {
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
