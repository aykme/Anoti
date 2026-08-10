package com.alekseivinogradov.anoti.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di.AnimeBaseBackgroundUpdateModule
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di.AnimePeriodicBackgroundUpdateModule
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.android.impl.presentation.di.AnimeBaseModule
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.AnimeDatabaseCompletedModule
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.di.AnimeNotificationModule
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.platform.impl.presentation.di.CelebrityAppModule
import com.alekseivinogradov.anoti.network.platform.impl.presentation.di.NetworkModule
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        CelebrityAppModule::class,
        AnimeDatabaseCompletedModule::class,
        NetworkModule::class,
        AnimeBaseModule::class,
        AnimeBaseBackgroundUpdateModule::class,
        AnimePeriodicBackgroundUpdateModule::class,
        AnimeNotificationModule::class
    ]
)
internal interface AppModule {
    companion object {
        @Provides
        fun prvideAnimeUpdateManager(
            coroutineContextProvider: CoroutineContextProvider,
            fetchAllAnimeDatabaseItemsUsecase: FetchAllAnimeDatabaseItemsUsecase,
            fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase,
            updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase,
            notificationManager: AnimeNotificationManager
        ): AnimeUpdateManager {
            return AnimeUpdateManagerImpl(
                coroutineContextProvider = coroutineContextProvider,
                fetchAllAnimeDatabaseItemsUsecase = fetchAllAnimeDatabaseItemsUsecase,
                fetchAnimeListByIdsUsecase = fetchAnimeListByIdsUsecase,
                updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase,
                notificationManager = notificationManager
            )
        }
    }
}
