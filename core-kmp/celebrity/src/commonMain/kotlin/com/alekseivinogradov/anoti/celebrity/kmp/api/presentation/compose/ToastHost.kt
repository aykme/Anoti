package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.controller.ToastController
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Shows [controller]'s messages as standard Material 3 snackbars. A newer message replaces the
 * one on screen instead of waiting behind it. The snackbar sits at the top start of [modifier]'s
 * bounds, so the caller places it.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun ToastHost(controller: ToastController, modifier: Modifier = Modifier) {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(controller, hostState) {
        controller.messages.collectLatest { message: StringResource ->
            hostState.showSnackbar(getString(message))
        }
    }
    SnackbarHost(hostState = hostState, modifier = modifier)
}
