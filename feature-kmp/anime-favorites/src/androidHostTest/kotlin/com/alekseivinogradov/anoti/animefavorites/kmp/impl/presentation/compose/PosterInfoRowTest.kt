package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.SPACING_UNIT_DP
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose.ITEM_MIN_HEIGHT_DP
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose.POSTER_WIDTH_FRACTION
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class PosterInfoRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    // The row composes the info twice, once only to measure it; the copy is never placed.
    private val isPlaced = SemanticsMatcher("is placed") { it.layoutInfo.isPlaced }

    private fun setRow(width: Dp, infoHeight: Dp, maxHeight: Dp = Dp.Unspecified) {
        composeRule.setContent {
            Box(Modifier.requiredWidth(width).heightIn(max = maxHeight)) {
                PosterInfoRow(
                    poster = { Box(Modifier.fillMaxSize().testTag(POSTER_TAG)) },
                    info = { Box(Modifier.fillMaxWidth().height(infoHeight).testTag(INFO_TAG)) }
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun bounds(tag: String): DpRect =
        composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
            .filterToOne(isPlaced)
            .getUnclippedBoundsInRoot()

    private fun DpRect.widthDp(): Float = (right - left).value

    private fun DpRect.heightDp(): Float = (bottom - top).value

    @Test
    fun thePosterTakesItsFractionAndTheInfoTheRestAfterTheSpacing() {
        //Given
        val width = ROW_WIDTH_DP.dp

        //When
        setRow(width, infoHeight = SHORT_INFO_HEIGHT_DP.dp)

        //Then
        val poster = bounds(POSTER_TAG)
        val info = bounds(INFO_TAG)
        assertEquals(ROW_WIDTH_DP * POSTER_WIDTH_FRACTION, poster.widthDp(), DP_TOLERANCE)
        assertEquals((poster.right + SPACING_UNIT_DP).value, info.left.value, DP_TOLERANCE)
        assertEquals(ROW_WIDTH_DP.toFloat(), (info.right - poster.left).value, DP_TOLERANCE)
    }

    @Test
    fun aShortInfoStillGivesBothTheMinimumHeight() {
        //Given
        val width = ROW_WIDTH_DP.dp

        //When
        setRow(width, infoHeight = SHORT_INFO_HEIGHT_DP.dp)

        //Then
        assertEquals(ITEM_MIN_HEIGHT_DP.toFloat(), bounds(POSTER_TAG).heightDp(), DP_TOLERANCE)
        assertEquals(ITEM_MIN_HEIGHT_DP.toFloat(), bounds(INFO_TAG).heightDp(), DP_TOLERANCE)
    }

    @Test
    fun aTallInfoStretchesThePosterToItsHeight() {
        //Given
        val width = ROW_WIDTH_DP.dp

        //When
        setRow(width, infoHeight = TALL_INFO_HEIGHT_DP.dp)

        //Then
        assertEquals(TALL_INFO_HEIGHT_DP.toFloat(), bounds(POSTER_TAG).heightDp(), DP_TOLERANCE)
    }

    @Test
    fun aRowNeverGrowsPastItsParent() {
        //Given
        val maxHeight = PARENT_MAX_HEIGHT_DP.dp

        //When
        setRow(ROW_WIDTH_DP.dp, infoHeight = TALL_INFO_HEIGHT_DP.dp, maxHeight = maxHeight)

        //Then
        assertEquals(PARENT_MAX_HEIGHT_DP.toFloat(), bounds(POSTER_TAG).heightDp(), DP_TOLERANCE)
        assertEquals(PARENT_MAX_HEIGHT_DP.toFloat(), bounds(INFO_TAG).heightDp(), DP_TOLERANCE)
    }
}

private const val POSTER_TAG = "poster"
private const val INFO_TAG = "info"
private const val ROW_WIDTH_DP = 400
private const val SHORT_INFO_HEIGHT_DP = 50
private const val TALL_INFO_HEIGHT_DP = 300

// Lower than the tall info, so the row has to stop short of it.
private const val PARENT_MAX_HEIGHT_DP = 200

// A pixel's rounding at Robolectric's density.
private const val DP_TOLERANCE = 1f
