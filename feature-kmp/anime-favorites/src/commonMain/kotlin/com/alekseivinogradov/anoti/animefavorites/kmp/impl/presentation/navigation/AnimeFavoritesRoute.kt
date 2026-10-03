package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose.AnimeFavoritesScreen

/**
 * Renders the anime-favorites screen of [screenComponent], reading its controller's state for as
 * long as the composition lasts and sending the screen's events back to it.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeFavoritesRoute(screenComponent: NavAnimeFavoritesScreenComponent) {
    val controller = screenComponent.controller
    val state by controller.state.collectAsState()
    val uiModel = remember(state) { mapStateToUiModel(state) }
    AnimeFavoritesScreen(
        uiModel = uiModel,
        dateFormatter = screenComponent.dateFormatter,
        dispatch = controller::accept
    )
}
