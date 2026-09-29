package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.ANIME_FAVORITES_TAB_TAG
import com.alekseivinogradov.anoti.main.impl.presentation.ANIME_LIST_TAB_TAG
import com.alekseivinogradov.anoti.main.impl.presentation.DiRootDependenciesFake
import com.alekseivinogradov.anoti.main.impl.presentation.RootLifecycle
import com.alekseivinogradov.anoti.main.impl.presentation.RootSession
import com.alekseivinogradov.anoti.main.impl.presentation.TestMainDispatcher
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SaveableStateCodec
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateFile
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateStorage
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.test.StandardTestDispatcher
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val SEARCH_BUTTON_TAG = "search_button"
private const val SEARCH_TEXT = "frieren"

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
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

    private val fileSystem = FakeFileSystem()

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
        fileSystem.checkNoOpenFiles()
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

    @Test
    fun theSearchTextComesBackInTheNextProcess() {
        //Given
        val shown = mutableStateOf(session)
        composeRule.setContent { RootSessionContent(session = shown.value) }
        typeSearchText()
        session.saveState()

        //When
        shown.value = createSession()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }

    @Test
    fun theSearchTextComesBackInTheNextProcessAfterATapRebuiltTheRoot() {
        //Given
        val shown = mutableStateOf(session)
        composeRule.setContent { RootSessionContent(session = shown.value) }
        composeRule.waitForIdle()
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).performClick()
        typeSearchText()
        session.saveState()

        //When
        shown.value = createSession()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }

    @Test
    fun everythingTheScreensSaveSurvivesTheCodec() {
        //Given
        composeRule.setContent { RootSessionContent(session = session) }
        typeSearchText()
        val saved = checkNotNull(session.currentRoot).saveableStateRegistry.performSave()

        //When
        val restored = SaveableStateCodec.decode(SaveableStateCodec.encode(saved))

        //Then
        assertTrue(saved.isNotEmpty())
        assertEquals(
            saved.mapValues { unwrapped(it.value) },
            restored.mapValues { unwrapped(it.value) }
        )
    }

    private fun typeSearchText() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_BUTTON_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextInput(SEARCH_TEXT)
        composeRule.waitForIdle()
    }

    // States compare by identity, so each is compared by what it holds.
    private fun unwrapped(value: Any?): Any? = when (value) {
        is MutableState<*> -> listOf("state", unwrapped(value.value))
        is List<*> -> value.map(::unwrapped)
        is Map<*, *> -> value.entries.associate { unwrapped(it.key) to unwrapped(it.value) }
        else -> value
    }

    private fun createSession() = RootSession(
        createDiRootComponent = { createDiRootComponent(parent = dependencies) },
        createLifecycle = ::createLifecycle,
        notificationPermissionRequests = requests,
        readNotificationPermissionStatus = {
            permissionReads++
            ASKABLE
        },
        savedStateStorage = SavedStateStorage(
            file = SavedStateFile(
                fileSystem = fileSystem,
                path = "/app/saved_state/root_saved_state.json".toPath()
            ),
            appVersion = "1.1 (10)",
            sceneSessionId = { "scene-session" }
        )
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
