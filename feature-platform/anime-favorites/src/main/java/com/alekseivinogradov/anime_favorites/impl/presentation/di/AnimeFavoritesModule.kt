package com.alekseivinogradov.anime_favorites.impl.presentation.di

import com.alekseivinogradov.anime_background_update.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anime_background_update.impl.presentation.di.AnimeOnceBackgroundUpdateModule
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.impl.data.source.AnimeFavoritesSourceImpl
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesExecutorFactory
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.network.api.data.SafeApi
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
