package com.alekseivinogradov.anoti.main.impl.presentation.orientation

import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIInterfaceOrientationMaskPortrait

/** The orientations a window of [width] by [height] points may take. */
internal fun orientationsOfAWindowSized(width: Double, height: Double): UIInterfaceOrientationMask =
    if (allowsRotation(width = width, height = height)) {
        UIInterfaceOrientationMaskAll
    } else {
        UIInterfaceOrientationMaskPortrait
    }
