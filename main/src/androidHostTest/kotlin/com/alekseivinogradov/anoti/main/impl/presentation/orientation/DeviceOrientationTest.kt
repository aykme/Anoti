package com.alekseivinogradov.anoti.main.impl.presentation.orientation

import android.content.pm.ActivityInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceOrientationTest {

    @Test
    fun aPhoneKeepsTheAppUpright() {
        //Given
        val widthPx = PHONE_WIDTH_PX
        val heightPx = PHONE_HEIGHT_PX

        //When
        val orientation = requestedOrientationFor(widthPx, heightPx, PHONE_DENSITY_DPI)

        //Then
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, orientation)
    }

    @Test
    fun aTabletLetsTheAppTurn() {
        //Given
        val widthPx = TABLET_WIDTH_PX
        val heightPx = TABLET_HEIGHT_PX

        //When
        val orientation = requestedOrientationFor(widthPx, heightPx, TABLET_DENSITY_DPI)

        //Then
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, orientation)
    }

    @Test
    fun aFoldableTurnsOnlyWhileUnfolded() {
        //Given
        val folded = FOLDED_WIDTH_PX to FOLDED_HEIGHT_PX
        val unfolded = UNFOLDED_WIDTH_PX to UNFOLDED_HEIGHT_PX

        //When
        val foldedOrientation =
            requestedOrientationFor(folded.first, folded.second, FOLDABLE_DENSITY_DPI)
        val unfoldedOrientation =
            requestedOrientationFor(unfolded.first, unfolded.second, FOLDABLE_DENSITY_DPI)

        //Then
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, foldedOrientation)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, unfoldedOrientation)
    }

    @Test
    fun aDisplayIsMeasuredAtTheDensityTheDeviceShipsWith() {
        //Given
        val justWide = EDGE_DENSITY_DPI
        val justNarrow = EDGE_DENSITY_DPI + 1

        //When
        val atWide = requestedOrientationFor(EDGE_WIDTH_PX, EDGE_HEIGHT_PX, justWide)
        val atNarrow = requestedOrientationFor(EDGE_WIDTH_PX, EDGE_HEIGHT_PX, justNarrow)

        //Then
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, atWide)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, atNarrow)
    }

    private companion object {
        // Pixel 4 XL.
        const val PHONE_WIDTH_PX = 1440
        const val PHONE_HEIGHT_PX = 3040
        const val PHONE_DENSITY_DPI = 560

        // Pixel Tablet.
        const val TABLET_WIDTH_PX = 2560
        const val TABLET_HEIGHT_PX = 1600
        const val TABLET_DENSITY_DPI = 320

        // Pixel 10 Pro Fold, whose two screens share one density.
        const val FOLDED_WIDTH_PX = 1080
        const val FOLDED_HEIGHT_PX = 2364
        const val UNFOLDED_WIDTH_PX = 2076
        const val UNFOLDED_HEIGHT_PX = 2152
        const val FOLDABLE_DENSITY_DPI = 390

        // At this density the smaller side is exactly 600 dp.
        const val EDGE_WIDTH_PX = 1200
        const val EDGE_HEIGHT_PX = 1920
        const val EDGE_DENSITY_DPI = 320
    }
}
