package com.alekseivinogradov.anoti.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponent
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * The Android app-scope graph: the root of the app's 3-tier `AppScope` → `ActivityScope` →
 * `FeatureScope` hierarchy, merging every `AppScope` contribution in the repo. Created once, in
 * `AnotiApp.onCreate`, via `DiAppComponent::class.create(appContext)`.
 *
 * @param appContext the application [PlatformContext] every app-scoped Android binding is built
 *   from.
 */
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class DiAppComponent(
    @get:Provides @AppContext val appContext: PlatformContext
) {
    /** Builds `MainActivity`'s activity-scope graph, see `:main`'s `DiRootComponent`. */
    abstract val diRootComponentFactory: DiRootComponent.Factory

    /**
     * Builds the app's notification channel, see `feature-kmp:anime-notification`'s
     * `AnimeNotificationChannelFactory`.
     */
    abstract val animeNotificationChannelFactory: AnimeNotificationChannelFactory

    /**
     * Schedules the periodic background update pass, see `feature-kmp:anime-background-update`'s
     * `DiAnimeBackgroundUpdatePlatformComponent`.
     */
    abstract val animeBackgroundScheduler: AnimeBackgroundScheduler
}
