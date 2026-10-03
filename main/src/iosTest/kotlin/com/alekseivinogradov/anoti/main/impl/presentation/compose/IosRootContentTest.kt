package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.DiRootDependenciesFake
import com.alekseivinogradov.anoti.main.impl.presentation.IosRootHolder
import com.alekseivinogradov.anoti.main.impl.presentation.RootHost
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SaveableStateCodec
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ANIME_FAVORITES_TAB_TAG = "anime_favorites_button"
private const val SEARCH_BUTTON_TAG = "search_button"
private const val SEARCH_TEXT = "frieren"

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class IosRootContentTest {

    private val scheduler = TestCoroutineScheduler()

    private lateinit var dependencies: DiRootDependenciesFake

    private val appLifecycles = mutableListOf<LifecycleRegistry>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        // The fake hands the main dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
    }

    @AfterTest
    fun tearDown() {
        appLifecycles.forEach { it.destroy() }
        Dispatchers.resetMain()
        assertEquals(listOf(), dependencies.animeDetailsRequests)
    }

    @Test
    fun theSearchTextSurvivesARebuiltComposition(): TestResult = runOnTheStoresClock {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)
        val composed = mutableStateOf(true)
        setContent { if (composed.value) Content(holder, root) }
        typeSearchText()
        composed.value = false
        waitForIdle()

        //When
        composed.value = true
        waitForIdle()

        //Then
        onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }

    @Test
    fun theSearchTextComesBackInTheNextProcess(): TestResult = runOnTheStoresClock {
        //Given
        val before = createHolder()
        val beforeRoot = before.rootFor(restoredState = null)
        val shown = mutableStateOf(before to beforeRoot)
        setContent { Content(shown.value.first, shown.value.second) }
        typeSearchText()
        val saved = before.saveState()

        //When
        val after = createHolder()
        shown.value = after to after.rootFor(restoredState = saved)
        waitForIdle()

        //Then
        onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }

    @Test
    fun aTabTapStillSwitchesScreensAfterTheCompositionIsRebuilt(): TestResult =
        runOnTheStoresClock {
            //Given
            val holder = createHolder()
            val root = holder.rootFor(restoredState = null)
            val composed = mutableStateOf(true)
            setContent { if (composed.value) Content(holder, root) }
            waitForIdle()
            composed.value = false
            waitForIdle()
            composed.value = true
            waitForIdle()

            //When
            onNodeWithTag(ANIME_FAVORITES_TAB_TAG).performClick()
            waitForIdle()

            //Then
            onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        }

    @Test
    fun everythingTheScreensSaveSurvivesTheCodec(): TestResult = runOnTheStoresClock {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)
        setContent { Content(holder, root) }
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

    // The composition shares the stores' clock. The test body runs on a clock of its own, since
    // the two must not share one.
    private fun runOnTheStoresClock(block: suspend ComposeUiTest.() -> Unit): TestResult =
        runComposeUiTest(
            effectContext = StandardTestDispatcher(scheduler),
            runTestContext = StandardTestDispatcher(TestCoroutineScheduler()),
            block = block
        )

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

    private fun ComposeUiTest.typeSearchText() {
        waitForIdle()
        onNodeWithTag(SEARCH_BUTTON_TAG).performClick()
        waitForIdle()
        onNode(hasSetTextAction()).performTextInput(SEARCH_TEXT)
        waitForIdle()
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
