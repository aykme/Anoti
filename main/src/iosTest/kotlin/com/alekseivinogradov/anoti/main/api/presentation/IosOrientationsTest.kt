package com.alekseivinogradov.anoti.main.api.presentation

import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import platform.UIKit.UIWindow
import kotlin.test.Test
import kotlin.test.assertEquals

// No test builds a real UIWindow: one made outside an app starts UIKit halfway, and the Compose
// tests in the same process then crash on text input.
class IosOrientationsTest {

    @Test
    fun withNoWindowThePlistListStands() {
        //Given
        val window: UIWindow? = null

        //When
        val mask = iosSupportedInterfaceOrientations(window)

        //Then
        assertEquals(UIInterfaceOrientationMaskAll, mask)
    }

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
