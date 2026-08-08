package com.alekseivinogradov.anoti.animefavorites.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.platform.impl.presentation.di.AnimeOnceBackgroundUpdateModule
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.AnimeFavoritesSourceImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module(includes = [AnimeOnceBackgroundUpdateModule::class])
interface AnimeFavoritesModule {
    companion object {
        @Provides
        fun provideAnimeFavoritesSource(
            service: ShikimoriApiService,
            safeApi: SafeApi
        ): AnimeFavoritesSource {
            return AnimeFavoritesSourceImpl(
                service = service,
                safeApi = safeApi
            )
        }

        @Provides
        fun provideFetchAnimeDetailsByIdUsecase(
            source: AnimeFavoritesSource
        ): FetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)

        @Provides
        fun provideFavoritesUsecases(
            updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase,
            fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
        ): FavoritesUsecases {
            return FavoritesUsecases(
                updateAllAnimeInBackgroundOnceUsecase = updateAllAnimeInBackgroundOnceUsecase,
                fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
            )
        }

        @Provides
        fun provideAnimeFavoritesExecutorFactory(
            coroutineContextProvider: CoroutineContextProvider,
            usecases: FavoritesUsecases,
            toastProvider: ToastProvider
        ): AnimeFavoritesExecutorFactory {
            return {
                AnimeFavoritesExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider,
                    usecases = usecases,
                    toastProvider = toastProvider
                )
            }
        }

        @Provides
        fun provideAnimeFavoritesMainStore(
            storeFactory: StoreFactory,
            executorFactory: AnimeFavoritesExecutorFactory
        ): AnimeFavoritesMainStore {
            return AnimeFavoritesMainStoreFactory(
                storeFactory = storeFactory,
                executorFactory = executorFactory
            ).create()
        }
    }
}
