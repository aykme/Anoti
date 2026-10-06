package com.alekseivinogradov.anoti.main.impl.presentation.orientation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import androidx.window.layout.WindowMetricsCalculator
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG

/**
 * The orientation [activity] asks for: any on a wide screen, portrait on a narrow one. The screen
 * is the whole display the activity is on, measured at the density the device ships with. The
 * user's display size setting therefore never locks a tablet upright.
 */
internal fun requestedOrientationOf(activity: Activity): Int {
    val bounds = WindowMetricsCalculator.getOrCreate()
        .computeMaximumWindowMetrics(activity)
        .bounds
    val orientation = requestedOrientationFor(
        widthPx = bounds.width(),
        heightPx = bounds.height(),
        stableDensityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
    )
    // Truncated, so a side logged as 600 really reaches the threshold.
    val widthDp = dpOf(bounds.width(), DisplayMetrics.DENSITY_DEVICE_STABLE).toInt()
    val heightDp = dpOf(bounds.height(), DisplayMetrics.DENSITY_DEVICE_STABLE).toInt()
    val decision =
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) "stays upright" else "turns"
    println(
        "$ANOTI_TAG DeviceOrientation: display ${widthDp}x$heightDp dp at stock density, " +
            "the app $decision"
    )
    return orientation
}

/**
 * The orientation for a display of [widthPx] by [heightPx] at [stableDensityDpi], the density
 * the device ships with.
 */
internal fun requestedOrientationFor(widthPx: Int, heightPx: Int, stableDensityDpi: Int): Int {
    val width = dpOf(widthPx, stableDensityDpi)
    val height = dpOf(heightPx, stableDensityDpi)
    return if (allowsRotation(width = width, height = height)) {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    } else {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

private fun dpOf(px: Int, densityDpi: Int): Double =
    px / (densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT)
