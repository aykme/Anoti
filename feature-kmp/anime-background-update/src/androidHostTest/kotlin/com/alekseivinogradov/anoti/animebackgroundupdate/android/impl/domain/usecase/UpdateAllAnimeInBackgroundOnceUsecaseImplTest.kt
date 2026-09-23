package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.usecase

import android.content.Context
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_ONCE_WORK_NAME
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_WORK_CONSTRAINTS
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class UpdateAllAnimeInBackgroundOnceUsecaseImplTest {

    private val appContext: Context = RuntimeEnvironment.getApplication()

    private lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            /* context = */
            appContext,
            /* configuration = */
            Configuration.Builder()
                .setWorkerFactory(AnimeUpdateWorker.Factory(AnimeUpdateManagerFake()))
                .build()
        )
        workManager = WorkManager.getInstance(appContext)
    }

    @AfterTest
    fun tearDown() {
        // Without this the connections WorkManager opened are still held when the test ends, and
        // the platform reports each one as leaked.
        WorkManagerTestInitHelper.closeWorkDatabase()
    }

    @Test
    fun askingForAnUpdateEnqueuesOneUnderTheSharedName() {
        //Given
        val usecase = createUsecase()

        //When
        usecase.execute()

        //Then
        assertEquals(1, enqueuedWork().size)
    }

    @Test
    fun askingAgainWhileAPassIsStillWaitingAddsNothing() {
        //Given
        val usecase = createUsecase()
        usecase.execute()
        val firstId = enqueuedWork().single().id

        //When
        usecase.execute()

        //Then
        // The favorites screen offers this on a button, so a row of taps must not queue a row
        // of passes over the same library.
        assertEquals(1, enqueuedWork().size)
        assertEquals(firstId, enqueuedWork().single().id)
    }

    private fun createUsecase() = UpdateAllAnimeInBackgroundOnceUsecaseImpl(
        workManager = { workManager },
        updateWork = OneTimeWorkRequestBuilder<AnimeUpdateWorker>()
            .setConstraints(ANIME_UPDATE_WORK_CONSTRAINTS)
            .build(),
        uniqueWorkName = ANIME_UPDATE_ONCE_WORK_NAME
    )

    private fun enqueuedWork(): List<WorkInfo> = workManager
        .getWorkInfosForUniqueWork(ANIME_UPDATE_ONCE_WORK_NAME)
        .get()
}
