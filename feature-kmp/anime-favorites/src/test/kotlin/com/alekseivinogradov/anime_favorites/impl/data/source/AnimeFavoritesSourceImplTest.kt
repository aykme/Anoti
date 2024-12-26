package com.alekseivinogradov.anime_favorites.impl.data.source

import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_favorites.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class AnimeFavoritesSourceImplTest {
    private val maxDelay = 60000 //1 minute
    private lateinit var safeApi: SafeApi
    private lateinit var service: ShikimoriApiService
    private lateinit var source: AnimeFavoritesSource

    @BeforeTest
    fun setup() {
        safeApi = SafeApiFake()
    }

    @Test
    fun testAnimeFavoritesSourceGetAnimeByIdSuccess() = runTest {
        //Given
        initServiceAndSource(desiredCallResult = DesiredCallResult.SUCCESS)
        val randomId = getRandomId()
        val expectedResult = service.getAnimeById(randomId).toListItemDomain()

        //When
        val result = source.getItemById(randomId)

        //Then
        assertTrue {
            result is CallResult.Success &&
                    result.value == expectedResult
        }

    }

    @Test
    fun testAnimeFavoritesSourceGetAnimeByIdError() = runTest {
        //Given
        initServiceAndSource(desiredCallResult = DesiredCallResult.OTHER_ERROR)
        val randomId = getRandomId()
        val expectedResult = try {
            service.getAnimeById(randomId)
            null
        } catch (e: Throwable) {
            CallResult.OtherError(e)
        }

        //When
        val result = source.getItemById(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                    result is CallResult.OtherError &&
                    result.throwable == expectedResult.throwable
        }
    }

    private fun initServiceAndSource(desiredCallResult: DesiredCallResult) {
        service = ShikimoriApiServiceImplFake(
            desiredCallResult = desiredCallResult,
            desiredDelay = Random.nextInt(maxDelay).milliseconds
        )
        source = AnimeFavoritesSourceImpl(
            service = service,
            safeApi = safeApi
        )
    }

    private fun getRandomId() = Random.nextInt(Int.MAX_VALUE)
}
