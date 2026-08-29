package com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.api.presentation.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// The icon slot's placeholder size in NotificationsRationaleDialogPreview; the real icon's size
// is the host's own decision, since the host also supplies the icon content.
val PREVIEW_ICON_SIZE_DP: Dp = 24.dp

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
const val PREVIEW_WIDTH_DP = 360
const val PREVIEW_HEIGHT_DP = 280
