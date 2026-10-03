package com.alekseivinogradov.anoti.main.api.presentation

import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIWindow
import kotlin.test.Test
import kotlin.test.assertEquals

// No test builds a real UIWindow: one made outside an app starts UIKit halfway, and the Compose
// tests in the same process then crash on text input.
class IosOrientationsTest {

    @Test
    fun withNoWindowEveryOrientationIsAllowed() {
        //Given
        val window: UIWindow? = null

        //When
        val mask = iosSupportedInterfaceOrientations(window)

        //Then
        assertEquals(UIInterfaceOrientationMaskAll, mask)
    }
}
