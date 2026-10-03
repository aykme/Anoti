package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.mapper.model.mapStateToUiModel
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose.AnimeListScreen

/**
 * Renders the anime-list screen of [screenComponent], reading its controller's state for as long
 * as the composition lasts and sending the screen's events back to it.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeListRoute(screenComponent: NavAnimeListScreenComponent) {
    val controller = screenComponent.controller
    val state by controller.state.collectAsState()
    val uiModel = remember(state) { mapStateToUiModel(state) }
    AnimeListScreen(
        uiModel = uiModel,
        dateFormatter = screenComponent.dateFormatter,
        dispatch = controller::accept
    )
}
