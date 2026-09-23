package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import androidx.work.Constraints
import androidx.work.NetworkType
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the work requests the component hands to WorkManager. Everything else it binds needs a
 * graph to build, and the pieces behind those bindings have tests of their own.
 */
@RunWith(RobolectricTestRunner::class)
class DiAnimeBackgroundUpdatePlatformComponentTest {

    private val component = object : DiAnimeBackgroundUpdatePlatformComponent {}

    @Test
    fun theHourlyUpdateRepeatsAtTheIntervalTheManagerNames() {
        //Given
        val request = component.provideAnimeUpdatePeriodicWork()

        //When
        val intervalMinutes = TimeUnit.MILLISECONDS
            .toMinutes(request.workSpec.intervalDuration)

        //Then
        assertEquals(AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES, intervalMinutes)
    }

    @Test
    fun bothUpdateRequestsWaitForAConnectionBeforeTheyRun() {
        //Given
        val periodic = component.provideAnimeUpdatePeriodicWork()
        val once = component.provideAnimeUpdateOnceWork()

        //When
        val networkTypes = listOf(periodic, once)
            .map { it.workSpec.constraints.requiredNetworkType }

        //Then
        assertEquals(listOf(NetworkType.CONNECTED, NetworkType.CONNECTED), networkTypes)
    }

    @Test
    fun bothUpdateRequestsNameTheWorkerTheFactoryBuilds() {
        //Given
        val periodic = component.provideAnimeUpdatePeriodicWork()
        val once = component.provideAnimeUpdateOnceWork()

        //When
        val named = listOf(periodic, once).map { it.workSpec.workerClassName }

        //Then
        // WorkManager stores this name and hands it back to the factory, which builds the
        // update worker for its own name and declines anything else. Another class here would
        // be declined and then looked for by reflection, which a shrunk build cannot satisfy.
        val expected = AnimeUpdateWorker::class.java.name
        assertEquals(listOf(expected, expected), named)
    }

    @Test
    fun neitherUpdateRequestWaitsForTheThingsThatWouldStallItForDays() {
        //Given
        val requests = listOf(
            component.provideAnimeUpdatePeriodicWork().workSpec.constraints,
            component.provideAnimeUpdateOnceWork().workSpec.constraints
        )

        //When
        val stalling = requests.map { constraints: Constraints ->
            listOf(
                constraints.requiresCharging(),
                constraints.requiresDeviceIdle(),
                constraints.requiresBatteryNotLow()
            )
        }

        //Then
        // A new episode is worth knowing about on a phone that is in use and not on a charger.
        assertEquals(listOf(listOf(false, false, false), listOf(false, false, false)), stalling)
    }
}
