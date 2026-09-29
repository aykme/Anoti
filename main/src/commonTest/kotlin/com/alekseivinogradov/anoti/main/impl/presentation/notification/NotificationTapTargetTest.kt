package com.alekseivinogradov.anoti.main.impl.presentation.notification

import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootNotificationTapComponent
import com.alekseivinogradov.anoti.main.impl.presentation.provider.AnimeNotificationTapPayloadProviderImpl
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationTapTargetTest {

    @Test
    fun thePayloadOfANewEpisodeNotificationOpensTheFavorites() {
        //Given
        val payload = AnimeNotificationTapPayloadProviderImpl().getNewEpisodeTapPayload()

        //When
        val target = notificationTapTarget(payload)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, target)
    }

    @Test
    fun theProviderTheAppIsWiredWithOpensTheFavorites() {
        //Given
        val provider = object : DiRootNotificationTapComponent {}
            .provideAnimeNotificationTapPayloadProvider(AnimeNotificationTapPayloadProviderImpl())

        //When
        val target = notificationTapTarget(provider.getNewEpisodeTapPayload())

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, target)
    }

    @Test
    fun aPayloadWithoutTheKeyOpensNothing() {
        //Given
        val payload = mapOf("another_key" to "AnimeFavorites")

        //When
        val target = notificationTapTarget(payload)

        //Then
        assertNull(target)
    }

    @Test
    fun aValueThatIsNotTextOpensNothing() {
        //Given
        val payload = mapOf(NOTIFICATION_TAP_TARGET_KEY to 1)

        //When
        val target = notificationTapTarget(payload)

        //Then
        assertNull(target)
    }

    @Test
    fun textThatNamesNoScreenOpensNothing() {
        //Given
        val payload = mapOf(NOTIFICATION_TAP_TARGET_KEY to "\"Settings\"")

        //When
        val target = notificationTapTarget(payload)

        //Then
        assertNull(target)
    }
}
