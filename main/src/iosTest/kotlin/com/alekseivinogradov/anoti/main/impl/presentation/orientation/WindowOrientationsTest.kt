package com.alekseivinogradov.anoti.main.impl.presentation.orientation

import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowOrientationsTest {

    @Test
    fun aPhoneSizedWindowKeepsTheAppUpright() {
        //Given
        val width = 402.0
        val height = 874.0

        //When
        val mask = orientationsOfAWindowSized(width = width, height = height)

        //Then
        assertEquals(UIInterfaceOrientationMaskPortrait, mask)
    }

    @Test
    fun aTabletSizedWindowTurnsEveryWay() {
        //Given
        val width = 820.0
        val height = 1180.0

        //When
        val mask = orientationsOfAWindowSized(width = width, height = height)

        //Then
        assertEquals(UIInterfaceOrientationMaskAll, mask)
    }
}
