package com.alekseivinogradov.anoti.animefavorites.kmp.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.AnimeFavoritesSourceImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Provides

/**
 * The anime-favorites screen's [FeatureScope] component. Provides the [AnimeFavoritesSource],
 * its usecases and the [AnimeFavoritesMainStore], on top of the bindings it inherits from
 * [parent]. [UpdateAllAnimeInBackgroundOnceUsecase] is consumed as a parameter here, not
 * provided — it comes from `feature-kmp:anime-background-update`'s app-scoped, platform-specific
 * binding, inherited from [parent] in turn.
 */
@Component
@FeatureScope
abstract class DiAnimeFavoritesComponent(
    @Component val parent: DiAnimeFavoritesDependencies
) {
    /** The app-wide [CoroutineContextProvider], inherited from the parent. */
    abstract val coroutineContextProvider: CoroutineContextProvider

    /** The [DateFormatter], inherited from the parent. */
    abstract val dateFormatter: DateFormatter

    /** The app-wide [AnimeDatabaseStore], inherited from the parent. */
    abstract val animeDatabaseStore: AnimeDatabaseStore

    /** The screen's [AnimeFavoritesMainStore]. */
    abstract val mainStore: AnimeFavoritesMainStore

    @Provides
    fun provideAnimeFavoritesSource(service: ShikimoriApiService, safeApi: SafeApi): AnimeFavoritesSource =
        AnimeFavoritesSourceImpl(service = service, safeApi = safeApi)

    @Provides
    fun provideFetchAnimeDetailsByIdUsecase(source: AnimeFavoritesSource): FetchAnimeDetailsByIdUsecase =
        FetchAnimeDetailsByIdUsecase(source)

    @Provides
    fun provideFavoritesUsecases(
        updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase,
        fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
    ): FavoritesUsecases = FavoritesUsecases(
        updateAllAnimeInBackgroundOnceUsecase = updateAllAnimeInBackgroundOnceUsecase,
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
    )

    @Provides
    fun provideAnimeFavoritesExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        usecases: FavoritesUsecases,
        toastProvider: ToastProvider
    ): AnimeFavoritesExecutorFactory = {
        AnimeFavoritesExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

    @Provides
    fun provideAnimeFavoritesMainStore(
        storeFactory: StoreFactory,
        executorFactory: AnimeFavoritesExecutorFactory
    ): AnimeFavoritesMainStore = AnimeFavoritesMainStoreFactory(storeFactory, executorFactory).create()
}

@KmpComponentCreate
expect fun createDiAnimeFavoritesComponent(
    parent: DiAnimeFavoritesDependencies
): DiAnimeFavoritesComponent
