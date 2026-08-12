package com.alekseivinogradov.anoti.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di.AnimeBaseBackgroundUpdateModule
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di.AnimePeriodicBackgroundUpdateModule
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.android.impl.presentation.di.AnimeBaseModule
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.android.impl.presentation.di.AnimeDatabaseCompletedModule
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.di.AnimeNotificationModule
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.main.impl.presentation.di.AnimeNotificationIntentProviderModule
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        AnimeDatabaseCompletedModule::class,
        TransitionalAppGraphBridgeModule::class,
        AnimeBaseModule::class,
        AnimeBaseBackgroundUpdateModule::class,
        AnimePeriodicBackgroundUpdateModule::class,
        AnimeNotificationModule::class,
        AnimeNotificationIntentProviderModule::class
    ]
)
internal interface AppModule {
    companion object {
        @Provides
        fun provideAnimeUpdateManager(
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
