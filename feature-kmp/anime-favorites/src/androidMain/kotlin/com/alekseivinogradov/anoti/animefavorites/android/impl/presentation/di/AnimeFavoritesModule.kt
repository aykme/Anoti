package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di.AnimeOnceBackgroundUpdateModule
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.scope.FeatureScope
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

/**
 * Bridges [AnimeFavoritesComponent]'s Dagger `inject()` target to `feature-kmp:anime-favorites`'s
 * kotlin-inject-anvil `FeatureScope` contributions (`AnimeFavoritesComponent`, commonMain), via
 * [AnimeFavoritesFeatureBridgeGraph]. Still needs to `include`
 * [AnimeOnceBackgroundUpdateModule] — the WorkManager-backed
 * [UpdateAllAnimeInBackgroundOnceUsecase] provider, which stays Dagger-only until Phase 9 and is
 * fed into the bridge graph as a parameter. All other provider logic (`AnimeFavoritesSource`,
 * its usecases, and the [AnimeFavoritesMainStore] binding) now lives in the commonMain component;
 * this module only constructs the bridge graph once per Dagger component build and reads the
 * terminal store off it. Retired in Phase 9 once `AnimeFavoritesComponent` here becomes a real
 * `@ContributesSubcomponent(FeatureScope::class)`.
 */
@Module(includes = [AnimeOnceBackgroundUpdateModule::class])
interface AnimeFavoritesModule {
    companion object {
        @Provides
        @FeatureScope
        fun provideAnimeFavoritesFeatureBridgeGraph(
            coroutineContextProvider: CoroutineContextProvider,
            storeFactory: StoreFactory,
            toastProvider: ToastProvider,
            shikimoriApiService: ShikimoriApiService,
            safeApi: SafeApi,
            updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase
        ): AnimeFavoritesFeatureBridgeGraph = AnimeFavoritesFeatureBridgeGraph::class.create(
            coroutineContextProvider,
            storeFactory,
            toastProvider,
            shikimoriApiService,
            safeApi,
            updateAllAnimeInBackgroundOnceUsecase
        )

        @Provides
        fun provideAnimeFavoritesMainStore(
            graph: AnimeFavoritesFeatureBridgeGraph
        ): AnimeFavoritesMainStore = graph.mainStore
    }
}
