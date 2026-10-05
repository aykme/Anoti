package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TWO_COLUMN_MIN_WIDTH_DP
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimeListGridCellsTest {

    @Test
    fun aListNarrowerThanTheThresholdHasOneColumn() {
        //Given
        val thresholdPx = THRESHOLD_PX

        //When
        val count = animeListColumnCount(availableWidthPx = 599, twoColumnMinWidthPx = thresholdPx)

        //Then
        assertEquals(1, count)
    }

    @Test
    fun aListExactlyAtTheThresholdHasTwoColumns() {
        //Given
        val thresholdPx = THRESHOLD_PX

        //When
        val count = animeListColumnCount(availableWidthPx = 600, twoColumnMinWidthPx = thresholdPx)

        //Then
        assertEquals(2, count)
    }

    @Test
    fun aListFarWiderThanTheThresholdStillHasTwoColumns() {
        //Given
        val thresholdPx = THRESHOLD_PX

        //When
        val count = animeListColumnCount(availableWidthPx = 2560, twoColumnMinWidthPx = thresholdPx)

        //Then
        assertEquals(2, count)
    }

    @Test
    fun aListWithNoWidthHasOneColumn() {
        //Given
        val thresholdPx = THRESHOLD_PX

        //When
        val count = animeListColumnCount(availableWidthPx = 0, twoColumnMinWidthPx = thresholdPx)

        //Then
        assertEquals(1, count)
    }

    @Test
    fun aNarrowListGetsOneCellAsWideAsItself() {
        //Given
        val density = Density(PHONE_DENSITY)

        //When
        val cells = with(AnimeListGridCells) { density.calculateCrossAxisCellSizes(1079, 0) }

        //Then
        assertEquals(listOf(1079), cells)
    }

    @Test
    fun aWideListGetsTheSameTwoCellsAsTwoFixedColumns() {
        //Given
        val density = Density(PHONE_DENSITY)
        val expected = with(GridCells.Fixed(2)) { density.calculateCrossAxisCellSizes(2237, 0) }

        //When
        val cells = with(AnimeListGridCells) { density.calculateCrossAxisCellSizes(2237, 0) }

        //Then
        assertEquals(expected, cells)
    }

    @Test
    fun aListExactlyAtTheThresholdAtAnInexactDensityHasTwoColumns() {
        //Given
        // Android computes the density as dpi * (1f / 160). At 336 dpi, 1260 px then reads back as a
        // hair under 600 dp. Pixels do not.
        val density = Density(INEXACT_DENSITY)
        val widthPx = 1260
        assertTrue(with(density) { widthPx.toDp() } < TWO_COLUMN_MIN_WIDTH_DP.dp)

        //When
        val cells = with(AnimeListGridCells) { density.calculateCrossAxisCellSizes(widthPx, 0) }

        //Then
        assertEquals(2, cells.size)
    }
}

private const val THRESHOLD_PX = 600

// 420 dpi, a common phone density.
private const val PHONE_DENSITY = 2.625f

// 336 dpi as Android computes it, one at which 600 dp does not survive a round trip through pixels.
private const val INEXACT_DENSITY = 336 * (1f / 160)
