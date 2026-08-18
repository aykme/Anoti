package com.alekseivinogradov.anoti.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di.DiAnimeBackgroundUpdatePlatformComponent
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.di.DiAnimeBackgroundUpdateComponent
import com.alekseivinogradov.anoti.animebase.kmp.impl.di.DiAnimeBaseComponent
import com.alekseivinogradov.anoti.animedatabase.android.impl.di.DiAnimeDatabasePlatformComponent
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.di.DiAnimeDatabaseComponent
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.di.DiAnimeNotificationPlatformComponent
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.anoti.celebrity.android.impl.di.DiCelebrityPlatformComponent
import com.alekseivinogradov.anoti.celebrity.kmp.impl.di.DiCelebrityComponent
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.main.impl.di.DiRootDependencies
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootPlatformComponent
import com.alekseivinogradov.anoti.network.android.impl.di.DiNetworkPlatformComponent
import com.alekseivinogradov.anoti.network.kmp.impl.di.DiNetworkComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * The Android app-wide component: the root of the `AppScope` → `RootScope` → `FeatureScope`
 * hierarchy. Created once, in `AnotiApp.onCreate`, via `DiAppComponent::class.create(appContext)`.
 *
 * @param appContext the application [PlatformContext] every app-scoped Android binding is built
 *   from.
 */
@Component
@AppScope
abstract class DiAppComponent(
    @get:Provides @AppContext val appContext: PlatformContext
) : DiNetworkComponent,
    DiNetworkPlatformComponent,
    DiCelebrityComponent,
    DiCelebrityPlatformComponent,
    DiAnimeDatabaseComponent,
    DiAnimeDatabasePlatformComponent,
    DiAnimeBaseComponent,
    DiAnimeBackgroundUpdateComponent,
    DiAnimeBackgroundUpdatePlatformComponent,
    DiAnimeNotificationPlatformComponent,
    DiRootPlatformComponent,
    DiRootDependencies {

    /** Builds the app's notification channel. */
    abstract val animeNotificationChannelFactory: AnimeNotificationChannelFactory

    /** Schedules the periodic background update pass. */
    abstract val animeBackgroundScheduler: AnimeBackgroundScheduler
}
