package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose.BottomNavigationBar

/**
 * Renders the bottom navigation bar from the root's bar controller, for as long as the composition
 * lasts. The selected tab comes from the store, which the root keeps on the screen the stack holds.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun BottomNavigationBarRoute(dependencies: RootDependencies) {
    val controller = dependencies.barController
    val state by controller.state.collectAsState()
    val uiModel = remember(state) { mapStateToUiModel(state) }
    BottomNavigationBar(uiModel = uiModel, dispatch = controller::accept)
}
