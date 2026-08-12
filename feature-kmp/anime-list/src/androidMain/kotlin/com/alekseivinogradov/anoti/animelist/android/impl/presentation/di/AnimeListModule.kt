package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.scope.FeatureScope
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

/**
 * Bridges [AnimeListComponent]'s Dagger `inject()` targets to `feature-kmp:anime-list`'s
 * kotlin-inject-anvil `FeatureScope` contributions (`AnimeListComponent`, commonMain), via
 * [AnimeListFeatureBridgeGraph]. All the actual provider logic (`AnimeListSource`, its usecases,
 * and the main/announced/ongoing/search section stores) now lives in that commonMain component;
 * this module only constructs the bridge graph once per Dagger component build and reads the
 * terminal stores off it. Retired in Phase 9 once `AnimeListComponent` here becomes a real
 * `@ContributesSubcomponent(FeatureScope::class)`.
 */
@Module
interface AnimeListModule {
    companion object {
        @Provides
        @FeatureScope
        fun provideAnimeListFeatureBridgeGraph(
            coroutineContextProvider: CoroutineContextProvider,
            storeFactory: StoreFactory,
            toastProvider: ToastProvider,
            shikimoriApiService: ShikimoriApiService,
            safeApi: SafeApi
        ): AnimeListFeatureBridgeGraph = AnimeListFeatureBridgeGraph::class.create(
            coroutineContextProvider,
            storeFactory,
            toastProvider,
            shikimoriApiService,
            safeApi
        )

        @Provides
        fun provideAnimeListMainStore(graph: AnimeListFeatureBridgeGraph): AnimeListMainStore =
            graph.mainStore

        @Provides
        fun provideOngoingSectionStore(graph: AnimeListFeatureBridgeGraph): OngoingSectionStore =
            graph.ongoingSectionStore

        @Provides
        fun provideAnnouncedSectionStore(graph: AnimeListFeatureBridgeGraph): AnnouncedSectionStore =
            graph.announcedSectionStore

        @Provides
        fun provideSearchSectionStore(graph: AnimeListFeatureBridgeGraph): SearchSectionStore =
            graph.searchSectionStore
    }
}
