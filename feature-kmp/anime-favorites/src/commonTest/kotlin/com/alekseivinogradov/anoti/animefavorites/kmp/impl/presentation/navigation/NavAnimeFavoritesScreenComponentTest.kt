package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.fake.UpdateAllAnimeInBackgroundOnceUsecaseFake
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animefavorites.kmp.api.di.DiAnimeFavoritesDependencies
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.createDiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NavAnimeFavoritesScreenComponentTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val lifecycles = mutableListOf<LifecycleRegistry>()

    private val catalogEngine = unreachableCatalog(testDispatcher)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        lifecycles.filter { it.state != Lifecycle.State.DESTROYED }.forEach { it.destroy() }
        Dispatchers.resetMain()
        assertTrue(
            catalogEngine.requestHistory.isEmpty(),
            "the favorites screen went to the catalog"
        )
    }

    private class DiAnimeFavoritesDependenciesFake(
        override val animeDatabaseStore: AnimeDatabaseStore,
        override val coroutineContextProvider: CoroutineContextProvider,
        catalogEngine: MockEngine
    ) : DiAnimeFavoritesDependencies {
        override val storeFactory: StoreFactory = DefaultStoreFactory()
        override val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = {},
            makeUnknownErrorSystemMessage = {}
        )
        override val dateFormatter: DateFormatter = DateFormatterFake()
        override val shikimoriApiService: ShikimoriApiService =
            ShikimoriApiServiceImpl(createHttpClient(catalogEngine))
        override val safeApi: SafeApi = SafeApiFake()
        override val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase =
            UpdateAllAnimeInBackgroundOnceUsecaseFake()
    }

    /** One component, the state keeper it saves through, and what its database reset reaches. */
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val stateKeeper: StateKeeperDispatcher,
        val component: NavAnimeFavoritesScreenComponent,
        val databaseUsecases: AnimeDatabaseUsecasesFake
    )

    private fun createWiring(savedState: SerializableContainer? = null): Wiring {
        val coroutineContextProvider = CoroutineContextProviderFake()
        val databaseUsecases = AnimeDatabaseUsecasesFake()
        val animeDatabaseStore = AnimeDatabaseStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = {
                AnimeDatabaseExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider,
                    usecases = databaseUsecases.usecases
                )
            }
        ).create()

        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        val stateKeeper = StateKeeperDispatcher(savedState)
        val component = NavAnimeFavoritesScreenComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper
            ),
            diAnimeFavoritesComponent = createDiAnimeFavoritesComponent(
                parent = DiAnimeFavoritesDependenciesFake(
                    animeDatabaseStore = animeDatabaseStore,
                    coroutineContextProvider = coroutineContextProvider,
                    catalogEngine = catalogEngine
                )
            )
        )
        lifecycle.resume()
        return Wiring(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            component = component,
            databaseUsecases = databaseUsecases
        )
    }

    @Test
    fun openingTheSectionPutsTheMainStoreIntoItsMinimumDurationLoadingState() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.component.openSectionUnlessRestored()

        //Then
        assertEquals(
            ContentTypeDomain.LOADING(hasMinimumDuration = true),
            wiring.component.mainStore.state.contentType
        )
    }

    @Test
    fun aFreshArrivalResetsTheDatabaseStoresExtraInfo() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.component.openSectionUnlessRestored()
        runCurrent()

        //Then
        assertEquals(1, wiring.databaseUsecases.resetExtraInfoCount)
    }

    @Test
    fun aComponentBuiltFromASavedMarkerKeepsTheDatabaseStoresExtraInfo() = runTest(testDispatcher) {
        //Given
        val beforeProcessDeath = createWiring()
        val savedState = beforeProcessDeath.stateKeeper.save()

        //When
        val afterProcessDeath = createWiring(savedState = savedState)
        afterProcessDeath.component.openSectionUnlessRestored()
        runCurrent()

        //Then
        assertEquals(
            0,
            afterProcessDeath.databaseUsecases.resetExtraInfoCount,
            "a restored screen must keep the extra info it was showing"
        )
        // The loading treatment is unconditional: only the reset above is skipped on a restore.
        assertEquals(
            ContentTypeDomain.LOADING(hasMinimumDuration = true),
            afterProcessDeath.component.mainStore.state.contentType
        )
    }

    @Test
    fun destroyingTheLifecycleDisposesBothStores() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.lifecycle.destroy()

        //Then
        assertTrue(wiring.component.mainStore.isDisposed, "the favorites store outlived its screen")
        assertTrue(
            wiring.component.animeDatabaseStore.isDisposed,
            "the database store outlived its screen"
        )
    }
}

/**
 * Answers nothing of use, since the favorites screen reads the device rather than the catalog.
 * Every request it does get is kept in its history, which the teardown asserts is empty.
 *
 * @param dispatcher what the engine answers on. Left to itself it picks `Dispatchers.IO`, which
 * takes the answer off the test's virtual clock and onto a second thread.
 */
private fun unreachableCatalog(dispatcher: CoroutineDispatcher) = MockEngine(
    MockEngineConfig().apply {
        this.dispatcher = dispatcher
        addHandler {
            respond(
                content = ByteReadChannel("[]"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }
)
