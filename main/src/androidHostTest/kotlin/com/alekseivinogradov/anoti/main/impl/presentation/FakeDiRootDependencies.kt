package com.alekseivinogradov.anoti.main.impl.presentation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.controller.SystemMessageController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.main.api.di.DiRootDependencies
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.core.rx.Disposable
import com.arkivanov.mvikotlin.core.rx.Observer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * The whole root graph built from fakes, so it can be created without a database, a network
 * client or a real dispatcher.
 */
internal class FakeDiRootDependencies : DiRootDependencies {

    /** Every store handed out through [animeDatabaseStore], in the order they were asked for. */
    val animeDatabaseStores = mutableListOf<FakeAnimeDatabaseStore>()

    override val storeFactory: StoreFactory = DefaultStoreFactory()
    override val coroutineContextProvider: CoroutineContextProvider = FakeCoroutineContextProvider()
    override val systemMessageProvider = SystemMessageProvider(
        makeConnectionErrorSystemMessage = {},
        makeUnknownErrorSystemMessage = {}
    )
    override val systemMessageController = SystemMessageController()
    override val dateFormatter: DateFormatter = FakeDateFormatter()
    override val shikimoriApiService: ShikimoriApiService = FakeShikimoriApiService()
    override val safeApi: SafeApi = FakeSafeApi()
    override val updateAllAnimeInBackgroundOnceUsecase = FakeUpdateAllAnimeInBackgroundOnceUsecase()

    // The real graph leaves this binding unscoped, so each consumer builds its own store. Mirrored
    // here, or the bar and the screens would share one and disposal would look global.
    override val animeDatabaseStore: AnimeDatabaseStore
        get() = FakeAnimeDatabaseStore().also(animeDatabaseStores::add)
}

/**
 * Routes every kind of work to whatever the test installed as the main dispatcher, so one virtual
 * clock drives all of it. Read on each access, not captured: the graph is built before the test
 * installs its dispatcher.
 */
internal class FakeCoroutineContextProvider : CoroutineContextProvider {

    override val mainCoroutineContext: CoroutineContext get() = Dispatchers.Main
    override val appMainCoroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    override fun newMainCoroutineContext(): CoroutineContext = SupervisorJob() + Dispatchers.Main

    override val workManagerCoroutineContext: CoroutineContext get() = Dispatchers.Main
    override val mainDispatcher: CoroutineDispatcher get() = Dispatchers.Main
    override val defaultDispatcher: CoroutineDispatcher get() = Dispatchers.Main
    override val ioDispatcher: CoroutineContext get() = Dispatchers.Main
    override val unconfinedDispatcher: CoroutineDispatcher get() = Dispatchers.Main
}

/** A store whose state only ever changes through [emit]. */
internal class FakeAnimeDatabaseStore : AnimeDatabaseStore {

    private val stateObservers = mutableListOf<Observer<AnimeDatabaseStore.State>>()
    private val labelObservers = mutableListOf<Observer<AnimeDatabaseStore.Label>>()

    override var state = AnimeDatabaseStore.State()
        private set

    override var isDisposed = false
        private set

    override fun init() = Unit

    override fun accept(intent: AnimeDatabaseStore.Intent) = Unit

    override fun states(observer: Observer<AnimeDatabaseStore.State>): Disposable {
        observer.onNext(state)
        stateObservers += observer
        return Disposable { stateObservers -= observer }
    }

    override fun labels(observer: Observer<AnimeDatabaseStore.Label>): Disposable {
        labelObservers += observer
        return Disposable { labelObservers -= observer }
    }

    // A disposed store completes both streams and keeps nobody subscribed, same as a real one.
    override fun dispose() {
        isDisposed = true
        stateObservers.toList().forEach(Observer<AnimeDatabaseStore.State>::onComplete)
        labelObservers.toList().forEach(Observer<AnimeDatabaseStore.Label>::onComplete)
        stateObservers.clear()
        labelObservers.clear()
    }

    /** Publishes [items] as the new saved-anime list to everyone subscribed. */
    fun emit(items: List<AnimeDbDomain>) {
        state = AnimeDatabaseStore.State(animeDatabaseItems = items)
        stateObservers.toList().forEach { it.onNext(state) }
    }
}

internal class FakeDateFormatter : DateFormatter {
    override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
}

/** Answers every listing with nothing, so the screens settle on their empty state. */
internal class FakeShikimoriApiService : ShikimoriApiService {

    override suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): List<AnimeShortResponse> = listOf()

    override suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse =
        error("No test opens an anime's details.")
}

/** Runs the call once and reports its outcome, without the real one's retries or delays. */
internal class FakeSafeApi : SafeApi {
    override suspend fun <T> call(apiCall: suspend () -> T): CallResult<T> =
        runCatching { apiCall() }.fold(
            onSuccess = { CallResult.Success(it) },
            onFailure = { CallResult.OtherError(it) }
        )
}

internal class FakeUpdateAllAnimeInBackgroundOnceUsecase : UpdateAllAnimeInBackgroundOnceUsecase {
    override fun execute() = Unit
}
