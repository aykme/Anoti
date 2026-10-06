package com.alekseivinogradov.anoti.main.impl.presentation.orientation

private const val ROTATING_WINDOW_MIN_SIDE = 600.0

/**
 * Whether a screen of [width] by [height] lets the app turn with the device. One whose smaller
 * side is under 600 keeps it upright, as on a phone. The sizes are in points on iOS and in dp on
 * Android.
 */
internal fun allowsRotation(width: Double, height: Double): Boolean =
    minOf(width, height) >= ROTATING_WINDOW_MIN_SIDE
