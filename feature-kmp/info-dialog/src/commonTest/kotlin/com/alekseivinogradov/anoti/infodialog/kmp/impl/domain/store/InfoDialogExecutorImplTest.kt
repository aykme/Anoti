package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.PRIVACY_POLICY_URL
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.SUPPORT_URL
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import com.alekseivinogradov.anoti.infodialog.kmp.impl.di.DiInfoDialogComponent
import com.alekseivinogradov.anoti.webpageopener.kmp.impl.domain.usecase.fake.OpenWebPageUsecaseFake
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InfoDialogExecutorImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val createdStores = mutableListOf<InfoDialogStore>()

    private var unknownErrors = 0

    private var connectionErrors = 0

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        createdStores.forEach { it.dispose() }
        Dispatchers.resetMain()
    }

    private fun createStore(
        opener: OpenWebPageUsecaseFake = OpenWebPageUsecaseFake()
    ): InfoDialogStore {
        val component = object : DiInfoDialogComponent {}
        return component.provideInfoDialogStore(
            storeFactory = DefaultStoreFactory(),
            executorFactory = component.provideInfoDialogExecutorFactory(
                coroutineContextProvider = CoroutineContextProviderFake(),
                usecases = component.provideInfoDialogUsecases(openWebPageUsecase = opener),
                systemMessageProvider = SystemMessageProvider(
                    makeConnectionErrorSystemMessage = { connectionErrors++ },
                    makeUnknownErrorSystemMessage = { unknownErrors++ }
                )
            )
        ).also(createdStores::add)
    }

    @Test
    fun infoClickShowsTheDialog() = runTest(testDispatcher) {
        //Given
        val store = createStore()

        //When
        store.accept(InfoDialogStore.Intent.InfoClick)

        //Then
        assertTrue(store.state.isShown)
    }

    @Test
    fun aSecondInfoClickWhileShownChangesNothing() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(InfoDialogStore.Intent.InfoClick)
        val emitted = mutableListOf<InfoDialogStore.State>()
        backgroundScope.launch { store.states.toList(emitted) }
        val emittedBefore = emitted.size

        //When
        store.accept(InfoDialogStore.Intent.InfoClick)

        //Then
        assertEquals(emittedBefore, emitted.size)
        assertTrue(store.state.isShown)
    }

    @Test
    fun closeClickHidesAShownDialog() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(InfoDialogStore.Intent.InfoClick)

        //When
        store.accept(InfoDialogStore.Intent.CloseClick)

        //Then
        assertFalse(store.state.isShown)
    }

    @Test
    fun closeClickWhileHiddenChangesNothing() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emitted = mutableListOf<InfoDialogStore.State>()
        backgroundScope.launch { store.states.toList(emitted) }
        val emittedBefore = emitted.size

        //When
        store.accept(InfoDialogStore.Intent.CloseClick)

        //Then
        assertEquals(emittedBefore, emitted.size)
        assertFalse(store.state.isShown)
    }

    @Test
    fun theDialogOpensAgainAfterItWasClosed() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(InfoDialogStore.Intent.InfoClick)
        store.accept(InfoDialogStore.Intent.CloseClick)

        //When
        store.accept(InfoDialogStore.Intent.InfoClick)

        //Then
        assertTrue(store.state.isShown)
    }

    @Test
    fun thePrivacyPolicyLinkOpensItsPageAndKeepsTheDialog() = runTest(testDispatcher) {
        //Given
        val opener = OpenWebPageUsecaseFake(result = true)
        val store = createStore(opener = opener)
        store.accept(InfoDialogStore.Intent.InfoClick)

        //When
        store.accept(InfoDialogStore.Intent.PrivacyPolicyClick)

        //Then
        assertEquals(listOf(PRIVACY_POLICY_URL), opener.openedUrls)
        assertTrue(store.state.isShown)
        assertEquals(0, unknownErrors)
    }

    @Test
    fun theSupportLinkOpensItsPageAndKeepsTheDialog() = runTest(testDispatcher) {
        //Given
        val opener = OpenWebPageUsecaseFake(result = true)
        val store = createStore(opener = opener)
        store.accept(InfoDialogStore.Intent.InfoClick)

        //When
        store.accept(InfoDialogStore.Intent.SupportClick)

        //Then
        assertEquals(listOf(SUPPORT_URL), opener.openedUrls)
        assertTrue(store.state.isShown)
        assertEquals(0, unknownErrors)
    }

    @Test
    fun aPrivacyPolicyPageNoAppOpensShowsTheUnknownErrorOnceAndKeepsTheDialog() =
        runTest(testDispatcher) {
            //Given
            val store = createStore(opener = OpenWebPageUsecaseFake(result = false))
            store.accept(InfoDialogStore.Intent.InfoClick)

            //When
            store.accept(InfoDialogStore.Intent.PrivacyPolicyClick)

            //Then
            assertEquals(1, unknownErrors)
            assertEquals(0, connectionErrors)
            assertTrue(store.state.isShown)
        }

    @Test
    fun aSupportPageNoAppOpensShowsTheUnknownErrorOnceAndKeepsTheDialog() =
        runTest(testDispatcher) {
            //Given
            val store = createStore(opener = OpenWebPageUsecaseFake(result = false))
            store.accept(InfoDialogStore.Intent.InfoClick)

            //When
            store.accept(InfoDialogStore.Intent.SupportClick)

            //Then
            assertEquals(1, unknownErrors)
            assertEquals(0, connectionErrors)
            assertTrue(store.state.isShown)
        }
}
