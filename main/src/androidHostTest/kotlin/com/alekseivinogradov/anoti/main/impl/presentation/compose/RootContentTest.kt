package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.ANIME_FAVORITES_TAB_TAG
import com.alekseivinogradov.anoti.main.impl.presentation.DiRootDependenciesFake
import com.alekseivinogradov.anoti.main.impl.presentation.RootHost
import com.alekseivinogradov.anoti.main.impl.presentation.TestMainDispatcher
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionSession
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class RootContentTest {

    private val mainDispatcher = TestMainDispatcher()

    // The rule shares the clock the stores run on.
    @get:Rule
    val composeRule = createComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    private lateinit var dependencies: DiRootDependenciesFake

    private val lifecycle = LifecycleRegistry()

    @BeforeTest
    fun setUp() {
        mainDispatcher.install()
        // The fake hands the main dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
    }

    @AfterTest
    fun tearDown() {
        lifecycle.destroy()
        mainDispatcher.remove()
        assertEquals(listOf(), dependencies.animeDetailsRequests)
    }

    @Test
    fun aTabTapStillSwitchesScreensAfterTheCompositionIsRebuilt() {
        //Given
        val host = RootHost(
            diRootComponent = createDiRootComponent(parent = dependencies),
            openingTarget = null,
            createComponentContext = { _: Boolean -> DefaultComponentContext(lifecycle) },
            notificationPermissionRequests = NotificationPermissionRequestsFake(),
            notificationPermissionSession = NotificationPermissionSession()
        )
        lifecycle.resume()
        val composed = mutableStateOf(true)
        composeRule.setContent {
            if (composed.value) {
                RootContent(
                    dependencies = host.dependencies,
                    notificationsRationale = host.notificationsRationale
                )
            }
        }
        composeRule.waitForIdle()
        composed.value = false
        composeRule.waitForIdle()
        composed.value = true
        composeRule.waitForIdle()

        //When
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
    }
}
