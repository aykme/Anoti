package com.alekseivinogradov.anoti.main.api.presentation

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import platform.UIKit.UIWindow
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalForeignApi::class)
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
        val window = UIWindow(frame = CGRectMake(0.0, 0.0, 402.0, 874.0))

        //When
        val mask = iosSupportedInterfaceOrientations(window)

        //Then
        assertEquals(UIInterfaceOrientationMaskPortrait, mask)
    }

    @Test
    fun aTabletSizedWindowTurnsEveryWay() {
        //Given
        val window = UIWindow(frame = CGRectMake(0.0, 0.0, 820.0, 1180.0))

        //When
        val mask = iosSupportedInterfaceOrientations(window)

        //Then
        assertEquals(UIInterfaceOrientationMaskAll, mask)
    }
}
