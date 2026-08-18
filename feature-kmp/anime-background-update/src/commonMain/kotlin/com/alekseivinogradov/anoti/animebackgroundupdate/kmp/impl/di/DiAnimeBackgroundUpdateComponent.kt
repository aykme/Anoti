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
 * Provides the local anime background-update source and its usecase bindings; mixed into
 * `DiAppComponent` on both platforms.
 *
 * Note: [UpdateAllAnimeInBackgroundOnceUsecase] is not provided here — each platform provides
 * its own implementation (WorkManager-backed on Android, coroutine-backed on iOS) from its own
 * `DiAnimeBackgroundUpdatePlatformComponent`.
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
