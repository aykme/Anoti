package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.THREE_COLUMN_MIN_WIDTH_DP
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TWO_COLUMN_MIN_WIDTH_DP
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimeListGridCellsTest {

    @Test
    fun aListNarrowerThanTheTwoColumnThresholdHasOneColumn() {
        //Given
        val widthPx = TWO_COLUMN_THRESHOLD_PX - 1

        //When
        val count = columnCount(widthPx)

        //Then
        assertEquals(1, count)
    }

    @Test
    fun aListExactlyAtTheTwoColumnThresholdHasTwoColumns() {
        //Given
        val widthPx = TWO_COLUMN_THRESHOLD_PX

        //When
        val count = columnCount(widthPx)

        //Then
        assertEquals(2, count)
    }

    @Test
    fun aListNarrowerThanTheThreeColumnThresholdHasTwoColumns() {
        //Given
        val widthPx = THREE_COLUMN_THRESHOLD_PX - 1

        //When
        val count = columnCount(widthPx)

        //Then
        assertEquals(2, count)
    }

    @Test
    fun aListExactlyAtTheThreeColumnThresholdHasThreeColumns() {
        //Given
        val widthPx = THREE_COLUMN_THRESHOLD_PX

        //When
        val count = columnCount(widthPx)

        //Then
        assertEquals(3, count)
    }

    @Test
    fun aListFarWiderThanTheThreeColumnThresholdStillHasThreeColumns() {
        //Given
        val widthPx = 2560

        //When
        val count = columnCount(widthPx)

        //Then
        assertEquals(3, count)
    }

    @Test
    fun aListWithNoWidthHasOneColumn() {
        //Given
        val widthPx = 0

        //When
        val count = columnCount(widthPx)

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
    fun aWiderListGetsTheSameThreeCellsAsThreeFixedColumns() {
        //Given
        // 1280 dp at 420 dpi, a tablet in landscape.
        val density = Density(PHONE_DENSITY)
        val expected = with(GridCells.Fixed(3)) { density.calculateCrossAxisCellSizes(3360, 0) }

        //When
        val cells = with(AnimeListGridCells) { density.calculateCrossAxisCellSizes(3360, 0) }

        //Then
        assertEquals(expected, cells)
    }

    @Test
    fun aListExactlyAtTheTwoColumnThresholdAtAnInexactDensityHasTwoColumns() {
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

    @Test
    fun aListExactlyAtTheThreeColumnThresholdAtAnInexactDensityHasThreeColumns() {
        //Given
        // At 336 dpi, 2310 px reads back as a hair under 1100 dp, as 1260 px does under 600 dp.
        val density = Density(INEXACT_DENSITY)
        val widthPx = 2310
        assertTrue(with(density) { widthPx.toDp() } < THREE_COLUMN_MIN_WIDTH_DP.dp)

        //When
        val cells = with(AnimeListGridCells) { density.calculateCrossAxisCellSizes(widthPx, 0) }

        //Then
        assertEquals(3, cells.size)
    }

    private fun columnCount(widthPx: Int): Int = animeListColumnCount(
        availableWidthPx = widthPx,
        twoColumnMinWidthPx = TWO_COLUMN_THRESHOLD_PX,
        threeColumnMinWidthPx = THREE_COLUMN_THRESHOLD_PX
    )
}

private const val TWO_COLUMN_THRESHOLD_PX = 600
private const val THREE_COLUMN_THRESHOLD_PX = 1100

// 420 dpi, a common phone density.
private const val PHONE_DENSITY = 2.625f

// 336 dpi as Android computes it, one at which neither threshold survives a round trip through
// pixels.
private const val INEXACT_DENSITY = 336 * (1f / 160)
