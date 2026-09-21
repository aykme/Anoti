package com.alekseivinogradov.anoti.main.impl.presentation.provider

import android.app.PendingIntent
import android.content.Intent
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import kotlinx.serialization.json.Json
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AnimeNotificationIntentProviderImplTest {

    @Test
    fun startsTheAppsOwnEntryPoint() {
        //Given
        val provider = AnimeNotificationIntentProviderImpl()

        //When
        val pendingIntent = provider.getNewEpisodeNotificationIntent(RuntimeEnvironment.getApplication())

        //Then
        assertTrue(shadowOf(pendingIntent).isActivity)
        assertEquals(
            MainActivity::class.java.name,
            shadowOf(pendingIntent).savedIntent.component?.className
        )
    }

    @Test
    fun tearsDownTheExistingTaskSoTheEntryPointIsEntered() {
        //Given
        val provider = AnimeNotificationIntentProviderImpl()

        //When
        val flags = shadowOf(
            provider.getNewEpisodeNotificationIntent(RuntimeEnvironment.getApplication())
        ).savedIntent.flags

        //Then
        assertTrue(flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertTrue(flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
    }

    @Test
    fun carriesAPayloadTheEntryPointReadsBackAsTheFavoritesScreen() {
        //Given
        val provider = AnimeNotificationIntentProviderImpl()

        //When
        val payload = shadowOf(
            provider.getNewEpisodeNotificationIntent(RuntimeEnvironment.getApplication())
        ).savedIntent.getStringExtra(MainActivity.EXTRA_DEEP_LINK_TARGET)

        //Then
        assertNotNull(payload)
        assertEquals(
            NavRootConfig.AnimeFavorites,
            Json.decodeFromString(NavRootConfig.serializer(), payload)
        )
    }

    @Test
    fun replacesAnAlreadyScheduledNotificationsPayloadAndStaysUnwritable() {
        //Given
        val provider = AnimeNotificationIntentProviderImpl()

        //When
        val flags = shadowOf(
            provider.getNewEpisodeNotificationIntent(RuntimeEnvironment.getApplication())
        ).flags

        //Then
        assertTrue(flags and PendingIntent.FLAG_UPDATE_CURRENT != 0)
        assertTrue(flags and PendingIntent.FLAG_IMMUTABLE != 0)
    }
}
