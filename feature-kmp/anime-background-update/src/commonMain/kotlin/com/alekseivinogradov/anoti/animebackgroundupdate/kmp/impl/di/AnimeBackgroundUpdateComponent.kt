package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source.AnimeBackgroundUpdateSourceImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the local anime background-update source and its usecase bindings to [AppScope]'s
 * merged component.
 *
 * Note: [UpdateAllAnimeInBackgroundOnceUsecase] (the interface implemented by the WorkManager
 * wrapper) is not provided here — its only implementation depends on `WorkManager`, which stays
 * on Dagger until Phase 9 alongside the rest of Android's scheduling machinery.
 */
@ContributesTo(AppScope::class)
interface AnimeBackgroundUpdateComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeBackgroundUpdateSource(
        service: ShikimoriApiService,
        safeApi: SafeApi
    ): AnimeBackgroundUpdateSource = AnimeBackgroundUpdateSourceImpl(service = service, safeApi = safeApi)

    @Provides
    @SingleIn(AppScope::class)
    fun provideFetchAnimeListByIdsUsecase(
        source: AnimeBackgroundUpdateSource
    ): FetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(source)
}
