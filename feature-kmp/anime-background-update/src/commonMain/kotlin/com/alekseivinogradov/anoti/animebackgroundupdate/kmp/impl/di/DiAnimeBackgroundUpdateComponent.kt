package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source.AnimeBackgroundUpdateSourceImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import me.tatarka.inject.annotations.Provides

/**
 * Contributes the local anime background-update source and its usecase bindings to [AppScope]'s
 * merged component.
 *
 * Note: [UpdateAllAnimeInBackgroundOnceUsecase] is not provided here — its only implementation
 * depends on `WorkManager`, so it is contributed from `androidMain`'s
 * `DiAnimeBackgroundUpdatePlatformComponent` instead, and has no iOS counterpart yet.
 */
interface DiAnimeBackgroundUpdateComponent {
    @Provides
    @AppScope
    fun provideAnimeBackgroundUpdateSource(
        service: ShikimoriApiService,
        safeApi: SafeApi
    ): AnimeBackgroundUpdateSource = AnimeBackgroundUpdateSourceImpl(service = service, safeApi = safeApi)

    @Provides
    @AppScope
    fun provideFetchAnimeListByIdsUsecase(
        source: AnimeBackgroundUpdateSource
    ): FetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(source)
}
