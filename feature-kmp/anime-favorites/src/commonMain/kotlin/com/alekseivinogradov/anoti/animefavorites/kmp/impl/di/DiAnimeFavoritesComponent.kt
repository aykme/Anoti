package com.alekseivinogradov.anoti.animefavorites.kmp.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.di.DiAnimeFavoritesDependencies
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Provides

/**
 * The anime-favorites screen's [FeatureScope] component. Provides its usecases and the
 * [AnimeFavoritesMainStore], on top of the bindings it inherits from [parent].
 * [UpdateAllAnimeInBackgroundOnceUsecase] is consumed as a parameter here, not provided — it
 * comes from `feature-kmp:anime-background-update`'s app-scoped, platform-specific binding,
 * inherited from [parent] in turn.
 */
@Component
@FeatureScope
abstract class DiAnimeFavoritesComponent(
    @Component val parent: DiAnimeFavoritesDependencies
) {
    /** The [DateFormatter], inherited from the parent. */
    abstract val dateFormatter: DateFormatter

    /** The app-wide [AnimeDatabaseStore], inherited from the parent. */
    abstract val animeDatabaseStore: AnimeDatabaseStore

    /** The screen's [AnimeFavoritesMainStore]. */
    abstract val mainStore: AnimeFavoritesMainStore

    @Provides
    fun provideFavoritesUsecases(
        updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase
    ): FavoritesUsecases = FavoritesUsecases(
        updateAllAnimeInBackgroundOnceUsecase = updateAllAnimeInBackgroundOnceUsecase
    )

    @Provides
    fun provideAnimeFavoritesExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        usecases: FavoritesUsecases
    ): AnimeFavoritesExecutorFactory = {
        AnimeFavoritesExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases
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
