package com.alekseivinogradov.anoti.main.impl.presentation.orientation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import androidx.window.layout.WindowMetricsCalculator

/**
 * The orientation [activity] asks for: any on a wide screen, portrait on a narrow one. The screen
 * is the whole display the activity is on, measured at the density the device ships with. The
 * user's display size setting therefore never makes a tablet narrow.
 */
internal fun requestedOrientationOf(activity: Activity): Int {
    val bounds = WindowMetricsCalculator.getOrCreate()
        .computeMaximumWindowMetrics(activity)
        .bounds
    return requestedOrientationFor(
        widthPx = bounds.width(),
        heightPx = bounds.height(),
        stableDensityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
    )
}

/**
 * The orientation for a display of [widthPx] by [heightPx] at [stableDensityDpi], the density
 * the device ships with.
 */
internal fun requestedOrientationFor(widthPx: Int, heightPx: Int, stableDensityDpi: Int): Int {
    val pxPerDp = stableDensityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT
    return if (allowsRotation(width = widthPx / pxPerDp, height = heightPx / pxPerDp)) {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    } else {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
