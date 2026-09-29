package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RememberedRoot
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateFile
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateStorage
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class RootSessionTest {

    private val scheduler = TestCoroutineScheduler()

    private lateinit var dependencies: DiRootDependenciesFake

    private val requests = NotificationPermissionRequestsFake()

    private val lifecycles = mutableListOf<LifecycleRegistry>()

    private var permissionStatus = CompletableDeferred(ASKABLE)

    private val fileSystem = FakeFileSystem()

    private val savedStatePath = "/app/saved_state/root_saved_state.json".toPath()

    private var sceneSessionId: String? = FIRST_SESSION

    private lateinit var session: RootSession

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        // The fake hands its dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
        session = createSession()
    }

    @AfterTest
    fun tearDown() {
        lifecycles.forEach { it.destroy() }
        Dispatchers.resetMain()
        fileSystem.checkNoOpenFiles()
    }

    @Test
    fun theFirstRootOpensOnTheAnimeListAndBecomesTheCurrentOne() {
        //Given
        val generation = session.generation

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertSame(root, session.currentRoot)
        assertEquals(0, generation)
    }

    @Test
    fun endingARootDestroysItsLifecycleAndLeavesNoCurrentRoot() {
        //Given
        val root = session.createRoot()

        //When
        session.endRoot(root)

        //Then
        assertEquals(Lifecycle.State.DESTROYED, root.lifecycle.lifecycle.state)
        assertNull(session.currentRoot)
    }

    @Test
    fun endingARootThatIsNoLongerCurrentLeavesTheCurrentOneAlone() {
        //Given
        val first = session.createRoot()
        val second = session.createRoot()

        //When
        session.endRoot(first)

        //Then
        assertSame(second, session.currentRoot)
        assertEquals(Lifecycle.State.CREATED, second.lifecycle.lifecycle.state)
    }

    @Test
    fun aRootAbandonedBeforeItWasRememberedIsEndedToo() {
        //Given
        val remembered = RememberedRoot(session)

        //When
        remembered.onAbandoned()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, remembered.root.lifecycle.lifecycle.state)
        assertNull(session.currentRoot)
    }

    @Test
    fun aForgottenRootIsEnded() {
        //Given
        val remembered = RememberedRoot(session)
        remembered.onRemembered()

        //When
        remembered.onForgotten()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, remembered.root.lifecycle.lifecycle.state)
    }

    @Test
    fun aTapBeforeAnyRootOpensTheFirstRootOnItsScreenWithoutARebuild() {
        //Given
        session.openFromNotification(NavRootConfig.AnimeFavorites)

        //When
        val first = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, first.activeScreen)
        assertEquals(0, session.generation)
    }

    @Test
    fun theRootAfterTheOneATapOpenedNoLongerSeesTheTap() {
        //Given
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        session.endRoot(session.createRoot())

        //When
        val next = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, next.activeScreen)
    }

    @Test
    fun aTapWithALiveRootAsksForANewRootThatOpensOnItsScreen() {
        //Given
        val live = session.createRoot()

        //When
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        val next = session.createRoot()
        session.endRoot(live)

        //Then
        assertEquals(1, session.generation)
        assertEquals(NavRootConfig.AnimeFavorites, next.activeScreen)
        assertSame(next, session.currentRoot)
    }

    @Test
    fun aTapOnTheScreenAlreadyOpenStillRebuilds() {
        //Given
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        session.createRoot()

        //When
        session.openFromNotification(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(1, session.generation)
    }

    @Test
    fun theNextProcessReopensTheScreenTheStateWasSavedOn() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
    }

    @Test
    fun aSaveableValueComesBackFromTheNextRootsRegistryAsTheSameKind() {
        //Given
        saveWhileOn(
            screen = NavRootConfig.AnimeList,
            saveable = mapOf("search" to mutableStateOf("Frieren"), "scroll" to listOf(3, 40))
        )

        //When
        val registry = session.createRoot().saveableStateRegistry

        //Then
        val search = registry.consumeRestored("search")
        assertIs<MutableState<*>>(search)
        assertEquals("Frieren", search.value)
        assertEquals(listOf(3, 40), registry.consumeRestored("scroll"))
    }

    @Test
    fun savingWithoutARootWritesNothingAndKeepsAnOlderFile() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)
        val older = savedText()

        //When
        session.saveState()

        //Then
        assertEquals(older, savedText())
    }

    @Test
    fun savingWithoutASceneSessionWritesNothingAndKeepsAnOlderFile() {
        //Given
        session.createRoot()
        saveWhileOn(NavRootConfig.AnimeFavorites)
        val older = savedText()
        sceneSessionId = null

        //When
        session.saveState()

        //Then
        assertEquals(older, savedText())
    }

    @Test
    fun aSaveThatFailsLeavesNoFileEvenWhenAnOlderOneExisted() {
        //Given
        val root = session.createRoot()
        saveWhileOn(NavRootConfig.AnimeFavorites)
        root.stateKeeper.register(key = "refused", strategy = ThrowingSerializerFake) { 1 }

        //When
        session.saveState()

        //Then
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun everyRootTakesTheFileAndAFileOfGarbageStartsItFresh() {
        //Given
        fileSystem.createDirectories(checkNotNull(savedStatePath.parent))
        fileSystem.write(savedStatePath) { writeUtf8("not a saved state") }

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun aStateSavedInAnotherFormatIsDropped() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)
        rewriteSavedFile(field = "formatVersion", value = JsonPrimitive(2))

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun aStateSavedByAnotherAppVersionIsDropped() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)
        session = createSession(appVersion = "1.2 (11)")

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun aStateSavedInAnotherSceneSessionIsDropped() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)
        sceneSessionId = "second-session"

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun aRootBuiltWithNoSceneSessionIgnoresTheFileAndDeletesIt() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)
        sceneSessionId = null

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun aStateTheStateKeeperCannotReadStartsTheRootFresh() {
        //Given
        saveWhileOn(NavRootConfig.AnimeFavorites)
        rewriteSavedFile(field = "state", value = JsonPrimitive("bm90IGEgc3RhdGU="))

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun aPendingTapDiscardsTheSavedState() {
        //Given
        saveWhileOn(
            screen = NavRootConfig.AnimeList,
            saveable = mapOf("search" to mutableStateOf("Frieren"))
        )
        session.openFromNotification(NavRootConfig.AnimeFavorites)

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertNull(root.saveableStateRegistry.consumeRestored("search"))
        assertFalse(fileSystem.exists(savedStatePath))
    }

    @Test
    fun thePermissionCheckActsOnTheStatusOfItsRoot() = runTest {
        //Given
        val root = session.createRoot()

        //When
        session.checkNotificationPermission(root)

        //Then
        assertEquals(1, requests.prompts)
    }

    @Test
    fun aStatusThatArrivesAfterItsRootEndedIsDropped() = runTest {
        //Given
        permissionStatus = CompletableDeferred()
        val root = session.createRoot()
        launch { session.checkNotificationPermission(root) }
        advanceUntilIdle()

        //When
        session.endRoot(root)
        session.createRoot()
        permissionStatus.complete(EXPLANATION_OWED)
        advanceUntilIdle()

        //Then
        assertFalse(root.host.notificationsRationale.visible.value)
    }

    @Test
    fun aStatusForARootReplacedWhileItWasReadIsDropped() = runTest {
        //Given
        permissionStatus = CompletableDeferred()
        val first = session.createRoot()
        launch { session.checkNotificationPermission(first) }
        advanceUntilIdle()
        val second = session.createRoot()

        //When
        permissionStatus.complete(ASKABLE)
        advanceUntilIdle()

        //Then
        assertEquals(0, requests.prompts)
        assertFalse(second.host.notificationsRationale.visible.value)
    }

    private fun createSession(appVersion: String = APP_VERSION) = RootSession(
        createDiRootComponent = { createDiRootComponent(parent = dependencies) },
        createLifecycle = ::createLifecycle,
        notificationPermissionRequests = requests,
        readNotificationPermissionStatus = { permissionStatus.await() },
        savedStateStorage = SavedStateStorage(
            file = SavedStateFile(fileSystem = fileSystem, path = savedStatePath),
            appVersion = appVersion,
            sceneSessionId = { sceneSessionId }
        )
    )

    /**
     * What a session left behind that saved while [screen] was open and its composition had
     * [saveable] in its registry, as the process before this one did.
     */
    private fun saveWhileOn(screen: NavRootConfig, saveable: Map<String, Any> = emptyMap()) {
        val earlier = createSession()
        val root = earlier.createRoot()
        root.host.dependencies.rootComponent.navigateTo(screen)
        saveable.forEach { (key: String, value: Any) ->
            root.saveableStateRegistry.registerProvider(key) { value }
        }
        earlier.saveState()
        earlier.endRoot(root)
    }

    private fun savedText(): String = fileSystem.read(savedStatePath) { readUtf8() }

    private fun rewriteSavedFile(field: String, value: JsonPrimitive) {
        val saved = Json.parseToJsonElement(fileSystem.read(savedStatePath) { readUtf8() })
        val rewritten = JsonObject(saved.jsonObject + (field to value))
        fileSystem.write(savedStatePath) { writeUtf8(rewritten.toString()) }
    }

    // Created at once, as iOS's own lifecycle settles right after the root is built. Ended the
    // way iOS ends its own, from any state.
    private fun createLifecycle(): RootLifecycle {
        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        lifecycle.create()
        return RootLifecycle(lifecycle = lifecycle, end = lifecycle::destroy)
    }

    private val SessionRoot.activeScreen: NavRootConfig
        get() = host.dependencies.rootComponent.childStack.value.active.configuration

    // Fails the save the way a screen's serializer would.
    private object ThrowingSerializerFake : KSerializer<Int> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ThrowingSerializerFake", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: Int) =
            throw SerializationException("refused")

        override fun deserialize(decoder: Decoder): Int = decoder.decodeInt()
    }

    private companion object {
        const val FIRST_SESSION = "first-session"
        const val APP_VERSION = "1.1 (10)"
        val ASKABLE = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = false
        )
        val EXPLANATION_OWED = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = true
        )
    }
}
