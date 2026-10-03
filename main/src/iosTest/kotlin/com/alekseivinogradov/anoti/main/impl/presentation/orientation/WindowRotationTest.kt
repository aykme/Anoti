package com.alekseivinogradov.anoti.main.impl.presentation.orientation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowRotationTest {

    @Test
    fun aPhoneSizedWindowKeepsTheAppUpright() {
        //Given
        val width = 402.0
        val height = 874.0

        //When
        val allows = allowsRotation(width = width, height = height)

        //Then
        assertFalse(allows)
    }

    @Test
    fun aWindowWhoseSmallerSideIsJustUnder600KeepsTheAppUpright() {
        //Given
        val width = 599.9
        val height = 1000.0

        //When
        val allows = allowsRotation(width = width, height = height)

        //Then
        assertFalse(allows)
    }

    @Test
    fun aWindowWhoseSmallerSideIs600LetsTheAppTurn() {
        //Given
        val width = 600.0
        val height = 1000.0

        //When
        val allows = allowsRotation(width = width, height = height)

        //Then
        assertTrue(allows)
    }

    @Test
    fun aLandscapeWindowIsMeasuredByItsSmallerSide() {
        //Given
        val narrowLandscape = 874.0 to 402.0
        val wideLandscape = 1180.0 to 820.0

        //When
        val narrowAllows = allowsRotation(narrowLandscape.first, narrowLandscape.second)
        val wideAllows = allowsRotation(wideLandscape.first, wideLandscape.second)

        //Then
        assertFalse(narrowAllows)
        assertTrue(wideAllows)
    }
}
