package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
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
import com.alekseivinogradov.anoti.main.impl.presentation.DiRootDependenciesFake
import com.alekseivinogradov.anoti.main.impl.presentation.IosRootHolder
import com.alekseivinogradov.anoti.main.impl.presentation.RootHost
import com.alekseivinogradov.anoti.main.impl.presentation.TestMainDispatcher
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SaveableStateCodec
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
import kotlin.test.assertTrue

private const val SEARCH_BUTTON_TAG = "search_button"
private const val SEARCH_TEXT = "frieren"

@RunWith(RobolectricTestRunner::class)
class IosRootContentTest {

    private val mainDispatcher = TestMainDispatcher()

    // The rule shares the clock the stores run on.
    @get:Rule
    val composeRule = createComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    private lateinit var dependencies: DiRootDependenciesFake

    private val appLifecycles = mutableListOf<LifecycleRegistry>()

    @BeforeTest
    fun setUp() {
        mainDispatcher.install()
        // The fake hands the main dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
    }

    @AfterTest
    fun tearDown() {
        appLifecycles.forEach { it.destroy() }
        mainDispatcher.remove()
        assertEquals(listOf(), dependencies.animeDetailsRequests)
    }

    @Test
    fun theSearchTextSurvivesARebuiltComposition() {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)
        val composed = mutableStateOf(true)
        composeRule.setContent { if (composed.value) Content(holder, root) }
        typeSearchText()
        composed.value = false
        composeRule.waitForIdle()

        //When
        composed.value = true
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }

    @Test
    fun theSearchTextComesBackInTheNextProcess() {
        //Given
        val before = createHolder()
        val beforeRoot = before.rootFor(restoredState = null)
        val shown = mutableStateOf(before to beforeRoot)
        composeRule.setContent { Content(shown.value.first, shown.value.second) }
        typeSearchText()
        val saved = before.saveState()

        //When
        val after = createHolder()
        shown.value = after to after.rootFor(restoredState = saved)
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }

    @Test
    fun aTabTapStillSwitchesScreensAfterTheCompositionIsRebuilt() {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)
        val composed = mutableStateOf(true)
        composeRule.setContent { if (composed.value) Content(holder, root) }
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

    @Test
    fun everythingTheScreensSaveSurvivesTheCodec() {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)
        composeRule.setContent { Content(holder, root) }
        typeSearchText()
        // A fresh registry starts from what the live one holds, so it reads them without
        // touching the composition.
        val saved = holder.newSaveableStateRegistry().performSave()

        //When
        val restored = SaveableStateCodec.decode(SaveableStateCodec.encode(saved))

        //Then
        assertTrue(saved.isNotEmpty())
        assertEquals(
            saved.mapValues { unwrapped(it.value) },
            restored.mapValues { unwrapped(it.value) }
        )
    }

    // Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
    // lowerCamelCase.
    @Suppress("FunctionNaming")
    @Composable
    private fun Content(holder: IosRootHolder, root: RootHost) {
        IosRootContent(
            holder = holder,
            dependencies = root.dependencies,
            notificationsRationale = root.notificationsRationale
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

    private fun createHolder(): IosRootHolder {
        val appLifecycle = LifecycleRegistry().also(appLifecycles::add)
        appLifecycle.resume()
        return IosRootHolder(
            createDiRootComponent = { createDiRootComponent(parent = dependencies) },
            appLifecycle = appLifecycle,
            appVersion = "1.1 (10)",
            notificationPermissionRequests = NotificationPermissionRequestsFake(),
            readNotificationPermissionStatus = { GRANTED }
        )
    }

    private companion object {
        val GRANTED = NotificationPermissionStatus(
            isAllowed = true,
            canPrompt = true,
            isExplanationOwed = false
        )
    }
}
