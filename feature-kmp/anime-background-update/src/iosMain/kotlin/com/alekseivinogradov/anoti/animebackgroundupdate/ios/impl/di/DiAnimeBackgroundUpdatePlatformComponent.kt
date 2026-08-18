package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler.AnimeBackgroundSchedulerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecaseImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Provides

/**
 * Contributes the iOS [AnimeUpdateManager], [AnimeBackgroundScheduler] and
 * [UpdateAllAnimeInBackgroundOnceUsecase] bindings to [AppScope]'s merged component — the mirror
 * of `androidMain`'s `DiAnimeBackgroundUpdatePlatformComponent`, minus the WorkManager plumbing
 * iOS has no use for.
 */
interface DiAnimeBackgroundUpdatePlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeUpdateManager(
        coroutineContextProvider: CoroutineContextProvider,
        fetchAllAnimeDatabaseItemsUsecase: FetchAllAnimeDatabaseItemsUsecase,
        fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase,
        updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase,
        notificationManager: AnimeNotificationManager
    ): AnimeUpdateManager = AnimeUpdateManagerImpl(
        coroutineContextProvider = coroutineContextProvider,
        fetchAllAnimeDatabaseItemsUsecase = fetchAllAnimeDatabaseItemsUsecase,
        fetchAnimeListByIdsUsecase = fetchAnimeListByIdsUsecase,
        updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase,
        notificationManager = notificationManager
    )

    /**
     * Registers the `BGAppRefreshTask` handler as soon as the scheduler is created. The task
     * identifier must also be listed in the iOS app target's Info.plist under
     * `BGTaskSchedulerPermittedIdentifiers` — see [AnimeBackgroundSchedulerImpl]'s KDoc.
     */
    @Provides
    @AppScope
    fun provideAnimeBackgroundScheduler(
        animeUpdateManager: AnimeUpdateManager
    ): AnimeBackgroundScheduler = AnimeBackgroundSchedulerImpl(
        animeUpdateManager = animeUpdateManager,
        coroutineScope = CoroutineScope(SupervisorJob())
    ).also { it.registerTaskHandler() }

    @Provides
    @AppScope
    fun provideUpdateAllAnimeInBackgroundOnceUsecase(
        animeUpdateManager: AnimeUpdateManager
    ): UpdateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseImpl(
        animeUpdateManager = animeUpdateManager,
        coroutineScope = CoroutineScope(SupervisorJob())
    )
}
