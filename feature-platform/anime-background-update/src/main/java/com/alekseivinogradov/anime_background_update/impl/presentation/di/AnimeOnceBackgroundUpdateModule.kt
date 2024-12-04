package com.alekseivinogradov.anime_background_update.impl.presentation.di

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anime_background_update.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecaseImpl
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.di.api.presentation.AnimeBackgroundUpdate
import com.alekseivinogradov.di.api.presentation.AppContext
import dagger.Module
import dagger.Provides

@Module
interface AnimeOnceBackgroundUpdateModule {
    companion object {
        @Provides
        @AnimeBackgroundUpdate
        fun provideAnimeUpdateOnceWork(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<AnimeUpdateWorker>().build()

        @Provides
        fun provideUpdateAllAnimeInBackgroundOnceUsecase(
            @AppContext context: Context,
            @AnimeBackgroundUpdate animeUpdateOnceWork: OneTimeWorkRequest
        ): UpdateAllAnimeInBackgroundOnceUsecase {
            return UpdateAllAnimeInBackgroundOnceUsecaseImpl(
                workManager = WorkManager.getInstance(context),
                updateWork = animeUpdateOnceWork,
                uniqueWorkName = AnimeUpdateWorker.animeUpdateOnceWorkName
            )
        }
    }
}
