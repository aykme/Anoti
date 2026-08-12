package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Android-only bridge graph that lets the still-Dagger `AnimeListComponent` (Fragment-facing,
 * same package) resolve `feature-kmp:anime-list`'s `FeatureScope` kotlin-inject-anvil
 * contributions (`AnimeListComponent`, commonMain) before `FeatureScope` has a real merge point
 * of its own (Phase 9's per-Fragment `@ContributesSubcomponent(FeatureScope::class)`). No iOS
 * counterpart is needed: nothing on iOS instantiates `AnimeListFragment` today.
 *
 * One instance is created per Dagger `AnimeListComponent` build (see `AnimeListModule`'s
 * `provideAnimeListFeatureBridgeGraph`, scoped `@FeatureScope` there so every store below is read
 * off the same instance), matching the original Dagger `@FeatureScope`'s one-instance-per-Fragment
 * lifetime. Retired in Phase 9 once the real `FeatureScope` merge point exists.
 *
 * @param coroutineContextProvider the app-wide [CoroutineContextProvider], sourced from Dagger's
 *   `MainComponent`.
 * @param storeFactory the app-wide [StoreFactory], sourced from Dagger's `MainComponent`.
 * @param toastProvider the app-wide [ToastProvider], sourced from Dagger's `MainComponent`.
 * @param shikimoriApiService the app-wide [ShikimoriApiService], sourced from Dagger's
 *   `MainComponent`.
 * @param safeApi the app-wide [SafeApi], sourced from Dagger's `MainComponent`.
 */
@MergeComponent(FeatureScope::class)
@SingleIn(FeatureScope::class)
abstract class AnimeListFeatureBridgeGraph(
    @get:Provides val coroutineContextProvider: CoroutineContextProvider,
    @get:Provides val storeFactory: StoreFactory,
    @get:Provides val toastProvider: ToastProvider,
    @get:Provides val shikimoriApiService: ShikimoriApiService,
    @get:Provides val safeApi: SafeApi
) {
    /** The [AnimeListMainStore], see commonMain's `AnimeListComponent`. */
    abstract val mainStore: AnimeListMainStore

    /** The [OngoingSectionStore], see commonMain's `AnimeListComponent`. */
    abstract val ongoingSectionStore: OngoingSectionStore

    /** The [AnnouncedSectionStore], see commonMain's `AnimeListComponent`. */
    abstract val announcedSectionStore: AnnouncedSectionStore

    /** The [SearchSectionStore], see commonMain's `AnimeListComponent`. */
    abstract val searchSectionStore: SearchSectionStore
}
