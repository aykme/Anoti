package com.alekseivinogradov.anoti.navigation.kmp

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavRootDeepLinkTest {

    @Test
    fun everyScreenSurvivesTheRoundTrip() {
        //Given
        val configs = listOf(NavRootConfig.AnimeList, NavRootConfig.AnimeFavorites)

        //When
        val decoded = configs.map { NavRootDeepLink.decode(NavRootDeepLink.encode(it)) }

        //Then
        assertEquals(configs, decoded)
    }

    @Test
    fun readsAPayloadWrittenByTheSerializerDirectly() {
        //Given
        val payload = Json.encodeToString(NavRootConfig.serializer(), NavRootConfig.AnimeFavorites)

        //When
        val decoded = NavRootDeepLink.decode(payload)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, decoded)
    }

    @Test
    fun aMissingPayloadNamesNoScreen() {
        //Given
        val payload: String? = null

        //When
        val decoded = NavRootDeepLink.decode(payload)

        //Then
        assertNull(decoded)
    }

    @Test
    fun textThatIsNotJsonNamesNoScreen() {
        //Given
        val payload = "not a payload"

        //When
        val decoded = NavRootDeepLink.decode(payload)

        //Then
        assertNull(decoded)
    }

    @Test
    fun jsonNamingAnUnknownScreenNamesNoScreen() {
        //Given
        val payload = """{"type":"AnimeSettings"}"""

        //When
        val decoded = NavRootDeepLink.decode(payload)

        //Then
        assertNull(decoded)
    }
}
