package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import androidx.work.NetworkType
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
    fun neitherUpdateRequestWaitsForTheThingsThatWouldStallItForDays() {
        //Given
        val periodic = component.provideAnimeUpdatePeriodicWork()

        //When
        val constraints = periodic.workSpec.constraints

        //Then
        // A new episode is worth knowing about on a phone that is in use and not on a charger.
        assertFalse(constraints.requiresCharging())
        assertFalse(constraints.requiresDeviceIdle())
        assertFalse(constraints.requiresBatteryNotLow())
    }
}
