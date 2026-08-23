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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list_image_description
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.LIST_LAST_ITEM_BOTTOM_PADDING_DP
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Grey700
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.LoadingSpinner
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.main_character_image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes

/**
 * The favorites list screen: loading spinner, empty-state panel, or the item list with
 * swipe-to-refresh, depending on [uiModel]'s [UiModel.contentType].
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeFavoritesScreen(
    uiModel: UiModel,
    dateFormatter: DateFormatter,
    topInsetDp: Dp = 0.dp,
    dispatch: (AnimeFavoritesMainStore.Intent) -> Unit
) {
    // Mirrors AnimeFavoritesViewImpl.setListItems: the original dispatches this from the
    // RecyclerView adapter's submitList completion callback regardless of which state is
    // currently visible — it's the only path that flips contentType LOADING -> LOADED, so it
    // can't be gated behind the ContentTypeUi.LOADED branch below.
    LaunchedEffect(uiModel.listItems) {
        if (uiModel.listItems.isNotEmpty()) {
            dispatch(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        }
    }

    when (uiModel.contentType) {
        ContentTypeUi.LOADING -> LoadingState()
        ContentTypeUi.EMPTY -> EmptyState()
        ContentTypeUi.LOADED -> ListState(
            uiModel = uiModel,
            dateFormatter = dateFormatter,
            topInsetDp = topInsetDp,
            dispatch = dispatch
        )
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

// Ported 1:1 from anime_favorites_empty_layout.xml: 130x130dp character image (8%-rounded,
// centerCrop) at the start, an 8dp gap, then a grey_700/10dp-rounded panel filling the rest,
// its text top-aligned with an 8dp margin on every side (vertical_bias="0" in the original).
// Text style matches TextAppearance.MaterialComponents.Subtitle2 (14sp, medium weight) —
// confirmed against the real com.google.android.material 1.14.0 style, not assumed.
@Suppress("FunctionNaming")
@Composable
private fun EmptyState() {
    // A plain Row(Modifier.height(IntrinsicSize.Min)) here would receive this screen's tight,
    // full-height constraints and get clamped right back up to full height. Box gives
    // non-matchParentSize children loose constraints, letting the Row shrink to its own
    // intrinsic (image) height and sit at the top, matching the original's wrap_content
    // layout hosted inside a match_parent FrameLayout.
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(CelebrityRes.drawable.main_character_image),
                contentDescription = stringResource(Res.string.empty_list_image_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(percent = EMPTY_IMAGE_CORNER_PERCENT))
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
                    fontSize = EMPTY_TEXT_SP.sp,
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
    uiModel: UiModel,
    dateFormatter: DateFormatter,
    topInsetDp: Dp,
    dispatch: (AnimeFavoritesMainStore.Intent) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            dispatch(AnimeFavoritesMainStore.Intent.UpdateSection)
            isRefreshing = false
        },
        state = pullToRefreshState,
        // Matches SwipeRefreshLayout.setColorSchemeResources(cinnabar_500) in the original —
        // PullToRefreshBox's default indicator color is onSurfaceVariant, not the theme's accent.
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                color = Cinnabar500
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .testTag("anime_favorites_rv")
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = topInsetDp,
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
                    }
                )
            }
        }
    }
}

private const val EMPTY_IMAGE_CORNER_PERCENT = 8
private const val EMPTY_TEXT_SP = 14
