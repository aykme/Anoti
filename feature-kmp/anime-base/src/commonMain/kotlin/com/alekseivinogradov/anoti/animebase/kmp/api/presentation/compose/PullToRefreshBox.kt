package com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White

/**
 * A full-size swipe-to-refresh surface: [content] fills it, with the pull indicator drawn on top.
 * Shared by every screen state that should let the user pull down to (re)trigger [onRefresh],
 * including a screen's error state.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun PullToRefreshBox(
    onRefresh: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                isRefreshing = false,
                state = pullToRefreshState,
                threshold = PULL_TO_REFRESH_THRESHOLD,
                onRefresh = onRefresh
            )
    ) {
        content()
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
