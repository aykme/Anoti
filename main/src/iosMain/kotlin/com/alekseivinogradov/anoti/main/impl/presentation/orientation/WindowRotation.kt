package com.alekseivinogradov.anoti.main.impl.presentation.orientation

// Android's own threshold for the same choice is sw600dp.
private const val ROTATING_WINDOW_MIN_SIDE = 600.0

/**
 * Whether a window of [width] by [height] points lets the app turn with the device. A window
 * whose smaller side is under 600 points keeps it upright, as Android keeps a screen below
 * sw600dp. iOS has no such rule of its own.
 */
internal fun allowsRotation(width: Double, height: Double): Boolean =
    minOf(width, height) >= ROTATING_WINDOW_MIN_SIDE
