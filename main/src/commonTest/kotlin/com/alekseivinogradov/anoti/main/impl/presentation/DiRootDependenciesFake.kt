package com.alekseivinogradov.anoti.main.impl.presentation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.fake.UpdateAllAnimeInBackgroundOnceUsecaseFake
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ANIME_LIST_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.fake.AnimeDatabaseStoreFake
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.controller.SystemMessageController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.alekseivinogradov.anoti.main.api.di.DiRootDependencies
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers

/**
 * The whole root graph built from fakes, so it can be created without a database, a network
 * client or a real dispatcher.
 */
internal class DiRootDependenciesFake : DiRootDependencies {

    private val catalogEngine = emptyCatalogEngine()

    /** Every store handed out through [animeDatabaseStore], in the order they were asked for. */
    val animeDatabaseStores = mutableListOf<AnimeDatabaseStoreFake>()

    /** The path of every details call a screen made. A test-driving the shell has none. */
    val animeDetailsRequests: List<String>
        get() = catalogEngine.requestHistory
            .map { it.url.encodedPath }
            .filterNot { it.endsWith("/$ANIME_LIST_APPEND_URL") }

    override val storeFactory: StoreFactory = DefaultStoreFactory()
    override val coroutineContextProvider: CoroutineContextProvider = CoroutineContextProviderFake(
        ioDispatcher = Dispatchers.Main,
        defaultDispatcher = Dispatchers.Main,
        workManagerCoroutineContext = Dispatchers.Main
    )
    override val systemMessageProvider = SystemMessageProvider(
        makeConnectionErrorSystemMessage = {},
        makeUnknownErrorSystemMessage = {}
    )
    override val systemMessageController = SystemMessageController()
    override val dateFormatter: DateFormatter = DateFormatterFake()
    override val shikimoriApiService: ShikimoriApiService =
        ShikimoriApiServiceImpl(createHttpClient(catalogEngine))
    override val safeApi: SafeApi = SafeApiFake()
    override val updateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseFake()

    // The real graph leaves this binding unscoped, so each consumer builds its own store. Mirrored
    // here, or the bar and the screens would share one and disposal would look global.
    override val animeDatabaseStore: AnimeDatabaseStore
        get() = AnimeDatabaseStoreFake().also(animeDatabaseStores::add)
}

/**
 * Answers every listing with nothing, so the screens settle on their empty state. A details call
 * gets the same, and is caught by asserting on [DiRootDependenciesFake.animeDetailsRequests].
 *
 * It answers on the main dispatcher the test installed. Left to itself it would pick
 * `Dispatchers.IO`, taking the answer off the virtual clock and onto a second thread.
 */
private fun emptyCatalogEngine() = MockEngine(
    MockEngineConfig().apply {
        dispatcher = Dispatchers.Main
        addHandler {
            respond(
                content = ByteReadChannel("[]"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }
)
