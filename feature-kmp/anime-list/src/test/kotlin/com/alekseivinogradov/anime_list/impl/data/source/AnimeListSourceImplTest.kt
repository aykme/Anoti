package com.alekseivinogradov.anime_list.impl.data.source

import com.alekseivinogradov.anime_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_base.api.data.model.SortData
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.impl.data.service.fake.ShikimoriApiServiceImplFake
import com.alekseivinogradov.anime_list.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult
import com.alekseivinogradov.network.api.domain.model.test.DesiredCallResult
import com.alekseivinogradov.network.impl.data.fake.SafeApiFake
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class AnimeListSourceImplTest {
    private val maxDelay = 60000 //1 minute
    private val page = 1
    private val searchText = ""
    private lateinit var safeApi: SafeApi
    private lateinit var service: ShikimoriApiService
    private lateinit var source: AnimeListSource

    @BeforeTest
    fun setup() {
        safeApi = SafeApiFake()
    }

    @Test
    fun testAnimeListSourceGetItemByIdSuccess() = runTest {
        //Given
        initServiceAndSource(desiredCallResult = DesiredCallResult.SUCCESS)
        val randomId: Int = createRandomId()
        val expectedResult: ListItemDomain = service.getAnimeById(randomId).toListItemDomain()

        //When
        val actualResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //Then
        assertTrue {
            actualResult is CallResult.Success &&
                    actualResult.value == expectedResult
        }

    }

    @Test
    fun testAnimeListSourceGetItemByIdError() = runTest {
        //Given
        initServiceAndSource(desiredCallResult = DesiredCallResult.OTHER_ERROR)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult.OtherError? = try {
            service.getAnimeById(randomId)
            null
        } catch (e: Throwable) {
            CallResult.OtherError(e)
        }

        //When
        val actualResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //Then
        assertTrue {
            expectedResult != null &&
                    actualResult is CallResult.OtherError &&
                    actualResult.throwable == expectedResult.throwable
        }
    }

    @Test
    fun testAnimeListSourceGetOngoingListSuccess() = runTest {
        //Given
        initServiceAndSource(desiredCallResult = DesiredCallResult.SUCCESS)
        val sort = SortData.SCORE
        val expectedResult: List<ListItemDomain> = service.getAnimeList(
            page = page,
            releaseStatus = ReleaseStatusData.ONGOING.value,
            sort = sort.value,
            search = searchText,
            ids = null
        ).map {
            it.toListItemDomain()
        }

        //When
        val actualResult: CallResult<List<ListItemDomain>> = source.getOngoingList(
            page = page,
            sort = sort
        )

        //Then
        assertTrue {
            actualResult is CallResult.Success &&
                    actualResult.value == expectedResult
        }
    }

    private fun initServiceAndSource(desiredCallResult: DesiredCallResult) {
        service = ShikimoriApiServiceImplFake(
            desiredCallResult = desiredCallResult,
            desiredDelay = Random.nextInt(maxDelay).milliseconds
        )
        source = AnimeListSourceImpl(
            service = service,
            safeApi = safeApi
        )
    }

    private fun createRandomId(): Int = Random.nextInt(Int.MAX_VALUE)
}