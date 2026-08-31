package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * System-bars insets for [RootContent], bundled together since the platform layer always
 * computes and passes both as one unit.
 *
 * @param horizontalSystemBarsPadding system-bars padding for the sides only — the platform
 * layer decides whether/how to apply it (e.g. Android's edge-to-edge API-level gate), this just
 * carries the result.
 * @param topInsetDp how far a screen's own top edge should be padded so it isn't drawn under the
 * status bar — same platform-decided-result reasoning as [horizontalSystemBarsPadding].
 */
internal class RootInsets(
    val horizontalSystemBarsPadding: Modifier,
    val topInsetDp: Dp
)
