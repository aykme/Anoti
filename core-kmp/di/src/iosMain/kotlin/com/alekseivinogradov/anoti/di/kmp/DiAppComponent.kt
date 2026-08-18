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
 * The app-wide DI graph for iOS. A host app creates it once, via `DiAppComponent::class.create()`,
 * then reads its accessors instead of constructing dependencies itself.
 *
 * No iOS host app exists in this repo yet, so nothing calls `create()` at runtime today. Merging
 * every `iosMain` binding here is what proves, at klib-compile time, that they actually wire
 * together — a role this class keeps once a host app ships, alongside being its entry point.
 */
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface DiAppComponent {
    /** The app-wide [HttpClient], see `core-kmp:network`'s `DiNetworkPlatformComponent`. */
    val httpClient: HttpClient

    /** The app-wide [SafeApi], see `core-kmp:network`'s `DiNetworkComponent`. */
    val safeApi: SafeApi

    /** The app-wide [StoreFactory], see `core-kmp:celebrity`'s `DiCelebrityComponent`. */
    val storeFactory: StoreFactory

    /**
     * The app-wide [CoroutineContextProvider], see `core-kmp:celebrity`'s
     * `DiCelebrityPlatformComponent`.
     */
    val coroutineContextProvider: CoroutineContextProvider

    /** The app-wide [ToastProvider], see `core-kmp:celebrity`'s `DiCelebrityPlatformComponent`. */
    val toastProvider: ToastProvider

    /** The [DateFormatter], see `core-kmp:celebrity`'s `DiCelebrityPlatformComponent`. */
    val dateFormatter: DateFormatter

    /**
     * The app-wide [AnimeDatabaseStore], see `core-kmp:anime-database`'s
     * `DiAnimeDatabaseComponent`.
     */
    val animeDatabaseStore: AnimeDatabaseStore

    /**
     * The app-wide [ShikimoriApiService], see `feature-kmp:anime-base`'s `DiAnimeBaseComponent`.
     */
    val shikimoriApiService: ShikimoriApiService

    /**
     * The app-wide [AnimeNotificationManager], see `feature-kmp:anime-notification`'s iOS
     * `DiAnimeNotificationPlatformComponent`.
     */
    val animeNotificationManager: AnimeNotificationManager

    /**
     * The app-wide [AnimeBackgroundScheduler], see `feature-kmp:anime-background-update`'s iOS
     * `DiAnimeBackgroundSchedulerPlatformComponent`.
     */
    val animeBackgroundScheduler: AnimeBackgroundScheduler
}
