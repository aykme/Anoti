package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecaseImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.animeUpdateOnceWorkName
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.di.android.api.presentation.AnimeBackgroundUpdate
import com.alekseivinogradov.anoti.di.android.api.presentation.AppContext
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
                uniqueWorkName = animeUpdateOnceWorkName
            )
        }
    }
}
