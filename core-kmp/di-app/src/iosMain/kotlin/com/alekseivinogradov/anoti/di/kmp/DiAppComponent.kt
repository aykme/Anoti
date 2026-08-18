package com.alekseivinogradov.anoti.di.kmp

import com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.di.DiAnimeBackgroundUpdatePlatformComponent
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.di.DiAnimeBackgroundUpdateComponent
import com.alekseivinogradov.anoti.animebase.kmp.impl.di.DiAnimeBaseComponent
import com.alekseivinogradov.anoti.animedatabase.ios.impl.di.DiAnimeDatabasePlatformComponent
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.di.DiAnimeDatabaseComponent
import com.alekseivinogradov.anoti.animenotification.ios.impl.di.DiAnimeNotificationPlatformComponent
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.ios.impl.di.DiCelebrityPlatformComponent
import com.alekseivinogradov.anoti.celebrity.kmp.impl.di.DiCelebrityComponent
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.main.impl.di.DiRootDependencies
import com.alekseivinogradov.anoti.network.ios.impl.di.DiNetworkPlatformComponent
import com.alekseivinogradov.anoti.network.kmp.impl.di.DiNetworkComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * The iOS app-wide component: the root of the `AppScope` → `RootScope` → `FeatureScope`
 * hierarchy, the mirror of the Android `DiAppComponent` in this module's `androidMain`. A host app
 * creates it once, via `DiAppComponent::class.create(appContext)`.
 *
 * @param appContext the application [PlatformContext] app-scoped bindings are built from. No iOS
 *   binding reads it yet; it is here so both platforms are built the same way.
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
    DiRootDependencies {

    /** The app-wide [AnimeNotificationManager]. */
    abstract val animeNotificationManager: AnimeNotificationManager

    /** Schedules the periodic background update pass. */
    abstract val animeBackgroundScheduler: AnimeBackgroundScheduler
}
