package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.ActivityContext
import com.alekseivinogradov.anoti.di.kmp.scope.ActivityScope
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * `MainActivity`'s [ActivityScope] graph — one instance per Activity, created from the app-scope
 * graph through [Factory]. It merges every [ActivityScope] contribution in the app (the bottom
 * navigation bar store) and owns the per-screen [ActivityScope] child graphs.
 */
@ContributesSubcomponent(ActivityScope::class)
@SingleIn(ActivityScope::class)
interface MainComponent {
    /** The app-wide [StoreFactory], inherited from the app-scope graph. */
    val storeFactory: StoreFactory

    /** The app-wide [CoroutineContextProvider], inherited from the app-scope graph. */
    val coroutineContextProvider: CoroutineContextProvider

    /** The app-wide [ToastProvider], inherited from the app-scope graph. */
    val toastProvider: ToastProvider

    /** The [DateFormatter], inherited from the app-scope graph. */
    val dateFormatter: DateFormatter

    /** The app-wide [AnimeDatabaseStore], inherited from the app-scope graph. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The app-wide [ShikimoriApiService], inherited from the app-scope graph. */
    val shikimoriApiService: ShikimoriApiService

    /** The app-wide [SafeApi], inherited from the app-scope graph. */
    val safeApi: SafeApi

    /** The Activity's [BottomNavigationBarStore]. */
    val bottomNavigationBarStore: BottomNavigationBarStore

    /** Builds the anime-list screen's feature-scope graph. */
    val animeListComponentFactory: AnimeListComponent.Factory

    /** Builds the anime-favorites screen's feature-scope graph. */
    val animeFavoritesComponentFactory: AnimeFavoritesComponent.Factory

    /**
     * Builds [MainComponent]s; contributed to the [AppScope] graph. The [activityContext]
     * parameter is bound into the generated component automatically — kotlin-inject-anvil
     * forwards factory parameters as `@get:Provides` properties, so no annotation is needed here.
     */
    @ContributesSubcomponent.Factory(AppScope::class)
    interface Factory {
        fun createMainComponent(@ActivityContext activityContext: PlatformContext): MainComponent
    }
}
