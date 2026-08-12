package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import dagger.Module

/**
 * No-op shell kept only because `AppModule` still `includes` it. Its two usecase providers
 * (`AnimeBackgroundUpdateSource`, `FetchAnimeListByIdsUsecase`) migrated to kotlin-inject-anvil's
 * `AnimeBackgroundUpdateComponent` (commonMain) and are bridged back to Dagger via
 * `TransitionalAppGraphBridgeModule`. `AnimeOnceBackgroundUpdateModule`,
 * `AnimePeriodicBackgroundUpdateModule`, and `AnimeUpdateWorker` stay Dagger-wired until
 * Phase 9, since they all ultimately need `AnimeUpdateManager`.
 */
@Module
interface AnimeBaseBackgroundUpdateModule
