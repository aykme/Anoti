package com.alekseivinogradov.anoti.di.kmp

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Bridge graph consumed by `app`'s still-Dagger `AppComponentInternal` while the App-scope
 * migration is in progress (Phases 2-8). Grows by one accessor per phase; deleted whole in
 * Phase 9 once `app` itself becomes a real `@MergeComponent(AppScope::class)`.
 *
 * @param platformContext the app-scoped [PlatformContext], supplied by the Dagger side when
 *   constructing this graph via the generated `TransitionalAppGraph::class.create(...)`.
 */
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class TransitionalAppGraph(
    @get:Provides @AppContext val platformContext: PlatformContext
) {
    /** The app-wide [HttpClient], see `core-kmp:network`'s `NetworkPlatformComponent`. */
    abstract val httpClient: HttpClient

    /** The app-wide [SafeApi], see `core-kmp:network`'s `NetworkComponent`. */
    abstract val safeApi: SafeApi

    /** The app-wide [StoreFactory], see `core-kmp:celebrity`'s `CelebrityComponent`. */
    abstract val storeFactory: StoreFactory

    /**
     * The app-wide [CoroutineContextProvider], see `core-kmp:celebrity`'s
     * `CelebrityPlatformComponent`.
     */
    abstract val coroutineContextProvider: CoroutineContextProvider

    /** The app-wide [ToastProvider], see `core-kmp:celebrity`'s `CelebrityPlatformComponent`. */
    abstract val toastProvider: ToastProvider

    /**
     * The (temporarily app-scoped, see `core-kmp:celebrity`'s Phase 3 note) [DateFormatter], see
     * `core-kmp:celebrity`'s `CelebrityPlatformComponent`.
     */
    abstract val dateFormatter: DateFormatter
}
