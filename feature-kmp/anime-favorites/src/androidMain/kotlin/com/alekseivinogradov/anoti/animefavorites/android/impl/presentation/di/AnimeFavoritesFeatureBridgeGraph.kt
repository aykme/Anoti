package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Android-only bridge graph that lets the still-Dagger `AnimeFavoritesComponent`
 * (Fragment-facing, same package) resolve `feature-kmp:anime-favorites`'s `FeatureScope`
 * kotlin-inject-anvil contributions (`AnimeFavoritesComponent`, commonMain) before `FeatureScope`
 * has a real merge point of its own (Phase 9's per-Fragment
 * `@ContributesSubcomponent(FeatureScope::class)`). No iOS counterpart is needed: nothing on iOS
 * instantiates `AnimeFavoritesFragment` today.
 *
 * One instance is created per Dagger `AnimeFavoritesComponent` build (see
 * `AnimeFavoritesModule`'s `provideAnimeFavoritesFeatureBridgeGraph`, scoped `@FeatureScope`
 * there), matching the original Dagger `@FeatureScope`'s one-instance-per-Fragment lifetime.
 * Retired in Phase 9 once the real `FeatureScope` merge point exists.
 *
 * @param coroutineContextProvider the app-wide [CoroutineContextProvider], sourced from Dagger's
 *   `MainComponent`.
 * @param storeFactory the app-wide [StoreFactory], sourced from Dagger's `MainComponent`.
 * @param toastProvider the app-wide [ToastProvider], sourced from Dagger's `MainComponent`.
 * @param shikimoriApiService the app-wide [ShikimoriApiService], sourced from Dagger's
 *   `MainComponent`.
 * @param safeApi the app-wide [SafeApi], sourced from Dagger's `MainComponent`.
 * @param updateAllAnimeInBackgroundOnceUsecase the WorkManager-backed
 *   [UpdateAllAnimeInBackgroundOnceUsecase], still Dagger-only (sourced from
 *   `AnimeOnceBackgroundUpdateModule`) until Phase 9.
 */
@MergeComponent(FeatureScope::class)
@SingleIn(FeatureScope::class)
abstract class AnimeFavoritesFeatureBridgeGraph(
    @get:Provides val coroutineContextProvider: CoroutineContextProvider,
    @get:Provides val storeFactory: StoreFactory,
    @get:Provides val toastProvider: ToastProvider,
    @get:Provides val shikimoriApiService: ShikimoriApiService,
    @get:Provides val safeApi: SafeApi,
    @get:Provides val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase
) {
    /** The [AnimeFavoritesMainStore], see commonMain's `AnimeFavoritesComponent`. */
    abstract val mainStore: AnimeFavoritesMainStore
}
