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

    /** Every store handed out through [animeDatabaseStore], in the order they were asked for. */
    val animeDatabaseStores = mutableListOf<AnimeDatabaseStoreFake>()

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
        ShikimoriApiServiceImpl(createHttpClient(emptyCatalogEngine()))
    override val safeApi: SafeApi = SafeApiFake()
    override val updateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseFake()

    // The real graph leaves this binding unscoped, so each consumer builds its own store. Mirrored
    // here, or the bar and the screens would share one and disposal would look global.
    override val animeDatabaseStore: AnimeDatabaseStore
        get() = AnimeDatabaseStoreFake().also(animeDatabaseStores::add)
}

/**
 * Routes every kind of work to whatever the test installed as the main dispatcher, so one virtual
 * clock drives all of it. Read on each access, not captured: the graph is built before the test
 * installs its dispatcher.
 */
/** Answers every listing with nothing, so the screens settle on their empty state. */
/**
 * Answers every listing with nothing, so the screens settle on their empty state. A details
 * call is a mistake in a test that only drives the shell, so it fails loudly instead.
 */
private fun emptyCatalogEngine() = MockEngine { request ->
    if (request.url.encodedPath.endsWith("/$ANIME_LIST_APPEND_URL")) {
        respond(
            content = ByteReadChannel("[]"),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    } else {
        error("No test opens an anime's details.")
    }
}
