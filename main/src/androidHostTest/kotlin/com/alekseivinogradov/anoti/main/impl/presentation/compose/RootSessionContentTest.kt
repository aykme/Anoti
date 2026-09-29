package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.ANIME_FAVORITES_TAB_TAG
import com.alekseivinogradov.anoti.main.impl.presentation.DiRootDependenciesFake
import com.alekseivinogradov.anoti.main.impl.presentation.RootLifecycle
import com.alekseivinogradov.anoti.main.impl.presentation.RootSession
import com.alekseivinogradov.anoti.main.impl.presentation.TestMainDispatcher
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.essenty.lifecycle.Lifecycle
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
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
class RootSessionContentTest {

    private val mainDispatcher = TestMainDispatcher()

    // The rule shares the clock the stores run on.
    @get:Rule
    val composeRule = createComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    private lateinit var dependencies: DiRootDependenciesFake

    private val requests = NotificationPermissionRequestsFake()

    // One per root the content built, in order.
    private val lifecycles = mutableListOf<LifecycleRegistry>()

    private var permissionReads = 0

    private lateinit var session: RootSession

    @BeforeTest
    fun setUp() {
        mainDispatcher.install()
        // The fake hands the main dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
        session = createSession()
    }

    @AfterTest
    fun tearDown() {
        lifecycles.forEach { it.destroy() }
        mainDispatcher.remove()
        assertEquals(listOf(), dependencies.animeDetailsRequests)
    }

    @Test
    fun composingBuildsOneRootAndItIsTheCurrentOne() {
        //Given
        val composed = mutableStateOf(false)
        composeRule.setContent {
            if (composed.value) RootSessionContent(session = session)
        }

        //When
        composed.value = true
        composeRule.waitForIdle()

        //Then
        assertEquals(1, lifecycles.size)
        assertSame(lifecycles.single(), session.currentRoot?.lifecycle?.lifecycle)
    }

    @Test
    fun removingTheContentEndsTheRoot() {
        //Given
        val composed = mutableStateOf(true)
        composeRule.setContent {
            if (composed.value) RootSessionContent(session = session)
        }
        composeRule.waitForIdle()

        //When
        composed.value = false
        composeRule.waitForIdle()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, lifecycles.single().state)
        assertNull(session.currentRoot)
    }

    @Test
    fun aTapEndsTheRootShownAndShowsANewOneOnItsScreen() {
        //Given
        composeRule.setContent { RootSessionContent(session = session) }
        composeRule.waitForIdle()

        //When
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        composeRule.waitForIdle()

        //Then
        assertEquals(2, lifecycles.size)
        assertEquals(Lifecycle.State.DESTROYED, lifecycles.first().state)
        assertSame(lifecycles.last(), session.currentRoot?.lifecycle?.lifecycle)
        assertEquals(
            NavRootConfig.AnimeFavorites,
            session.currentRoot?.host?.dependencies?.rootComponent?.childStack?.value?.active
                ?.configuration
        )
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
    }

    @Test
    fun eachRootsPermissionStatusIsAppliedOnce() {
        //Given
        val composed = mutableStateOf(true)
        composeRule.setContent {
            if (composed.value) RootSessionContent(session = session)
        }
        composeRule.waitForIdle()

        //When
        composed.value = false
        composeRule.waitForIdle()
        composed.value = true
        composeRule.waitForIdle()

        //Then
        assertEquals(2, lifecycles.size)
        assertEquals(2, permissionReads)
        assertEquals(2, requests.prompts)
    }

    private fun createSession() = RootSession(
        createDiRootComponent = { createDiRootComponent(parent = dependencies) },
        createLifecycle = ::createLifecycle,
        notificationPermissionRequests = requests,
        readNotificationPermissionStatus = {
            permissionReads++
            ASKABLE
        }
    )

    // Resumed at once, as iOS's own lifecycle is once the app is in front.
    private fun createLifecycle(): RootLifecycle {
        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        lifecycle.resume()
        return RootLifecycle(lifecycle = lifecycle, end = lifecycle::destroy)
    }

    private companion object {
        val ASKABLE = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = false
        )
    }
}
