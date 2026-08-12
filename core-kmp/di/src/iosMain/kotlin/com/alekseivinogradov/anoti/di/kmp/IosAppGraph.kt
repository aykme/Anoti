package com.alekseivinogradov.anoti.di.kmp

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import io.ktor.client.HttpClient
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * The iOS app-scope graph — iOS's counterpart of Android's `AppGraph` (`:app`) and the entry
 * point a future iOS host app creates once, via `IosAppGraph::class.create()`.
 *
 * Until that host app exists this class also earns its keep as the thing that actually merges
 * and type-checks every `iosMain` binding in the repo at klib-compile time; without it the iOS
 * actuals would compile but never be proven to wire together.
 */
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface IosAppGraph {
    /** The app-wide [HttpClient], see `core-kmp:network`'s `NetworkPlatformComponent`. */
    val httpClient: HttpClient

    /** The app-wide [SafeApi], see `core-kmp:network`'s `NetworkComponent`. */
    val safeApi: SafeApi

    /** The app-wide [StoreFactory], see `core-kmp:celebrity`'s `CelebrityComponent`. */
    val storeFactory: StoreFactory

    /**
     * The app-wide [CoroutineContextProvider], see `core-kmp:celebrity`'s
     * `CelebrityPlatformComponent`.
     */
    val coroutineContextProvider: CoroutineContextProvider

    /** The app-wide [ToastProvider], see `core-kmp:celebrity`'s `CelebrityPlatformComponent`. */
    val toastProvider: ToastProvider

    /** The [DateFormatter], see `core-kmp:celebrity`'s `CelebrityPlatformComponent`. */
    val dateFormatter: DateFormatter

    /**
     * The app-wide [AnimeDatabaseStore], see `core-kmp:anime-database`'s `AnimeDatabaseComponent`.
     */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The app-wide [ShikimoriApiService], see `feature-kmp:anime-base`'s `AnimeBaseComponent`. */
    val shikimoriApiService: ShikimoriApiService

    /**
     * The app-wide [AnimeNotificationManager], see `feature-kmp:anime-notification`'s iOS
     * `AnimeNotificationPlatformComponent`.
     */
    val animeNotificationManager: AnimeNotificationManager

    /**
     * The app-wide [AnimeBackgroundScheduler], see `feature-kmp:anime-background-update`'s iOS
     * `AnimeBackgroundSchedulerPlatformComponent`.
     */
    val animeBackgroundScheduler: AnimeBackgroundScheduler
}
