package com.alekseivinogradov.anoti.main.api.presentation

import com.alekseivinogradov.anoti.main.impl.presentation.orientation.orientationsOfAWindowSized
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIWindow

/**
 * The orientations [window] may take: every one once its smaller side reaches 600 points,
 * upright only below that. With no window to measure, every orientation.
 *
 * It reads no app state, so it answers even before the app has started.
 */
@OptIn(ExperimentalForeignApi::class)
fun iosSupportedInterfaceOrientations(window: UIWindow?): UIInterfaceOrientationMask {
    val size = window?.bounds?.useContents { size.width to size.height }
        ?: return UIInterfaceOrientationMaskAll
    return orientationsOfAWindowSized(width = size.first, height = size.second)
}
