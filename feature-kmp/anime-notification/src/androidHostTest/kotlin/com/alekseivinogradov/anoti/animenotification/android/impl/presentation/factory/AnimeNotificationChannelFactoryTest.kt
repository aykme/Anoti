package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory

import android.app.NotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.anime_notification_channel
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.anime_notification_channel_description
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.getString
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AnimeNotificationChannelFactoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val coroutineContextProvider = CoroutineContextProviderFake(ioDispatcher = testDispatcher)

    private val factory = AnimeNotificationChannelFactory(coroutineContextProvider)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun theChannelIsTheOneTheNotificationsArePostedTo() = runTest {
        //Given
        val expectedId = CHANNEL_ID

        //When
        val channel = factory.create()

        //Then
        assertEquals(expectedId, channel.id)
    }

    @Test
    fun theChannelIsNamedAndDescribedForTheUser() = runTest {
        //Given
        val expectedName = getString(Res.string.anime_notification_channel)
        val expectedDescription = getString(Res.string.anime_notification_channel_description)

        //When
        val channel = factory.create()

        //Then
        assertEquals(expectedName, channel.name.toString())
        assertEquals(expectedDescription, channel.description)
    }

    @Test
    fun theChannelVibratesAtTheDefaultImportance() = runTest {
        //Given
        val expectedImportance = NotificationManager.IMPORTANCE_DEFAULT

        //When
        val channel = factory.create()

        //Then
        assertEquals(expectedImportance, channel.importance)
        assertTrue(channel.shouldVibrate())
    }
}
