package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.ImageResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val ORIGINAL_PATH = "/system/animes/original/1.jpg"
private const val PREVIEW_PATH = "/system/animes/preview/1.jpg"

class AnimeResponseToListItemDomainMapperTest {

    @Test
    fun theFullSizeImageIsPreferredOverThePreview() {
        //Given
        val imageResponse = ImageResponse(
            originalSizeUrl = ORIGINAL_PATH,
            previewSizeUrl = PREVIEW_PATH
        )

        //When
        val imageUrl = mapImageUrlDataToDomain(imageResponse)

        //Then
        assertEquals(SHIKIMORI_BASE_URL + ORIGINAL_PATH, imageUrl)
    }

    @Test
    fun thePreviewIsUsedWhenThereIsNoFullSizeImage() {
        //Given
        val imageResponse = ImageResponse(previewSizeUrl = PREVIEW_PATH)

        //When
        val imageUrl = mapImageUrlDataToDomain(imageResponse)

        //Then
        assertEquals(SHIKIMORI_BASE_URL + PREVIEW_PATH, imageUrl)
    }

    @Test
    fun anImageWithNeitherSizeYieldsNoUrl() {
        //Given
        val imageResponse = ImageResponse()

        //When
        val imageUrl = mapImageUrlDataToDomain(imageResponse)

        //Then
        assertNull(imageUrl)
    }

    @Test
    fun anAnimeWithNoImageAtAllYieldsNoUrl() {
        //Given
        val imageResponse: ImageResponse? = null

        //When
        val imageUrl = mapImageUrlDataToDomain(imageResponse)

        //Then
        assertNull(imageUrl)
    }

    @Test
    fun everyReleaseStatusTheApiSendsHasItsOwnDomainStatus() {
        //Given
        val apiStatuses = ReleaseStatusData.entries

        //When
        val domainStatuses = apiStatuses.map { mapReleaseStatusDataToDomain(it.value) }

        //Then
        assertEquals(
            listOf(
                ReleaseStatusDomain.ONGOING,
                ReleaseStatusDomain.ANNOUNCED,
                ReleaseStatusDomain.RELEASED
            ),
            domainStatuses
        )
    }

    @Test
    fun aReleaseStatusTheAppDoesNotKnowBecomesUnknown() {
        //Given
        val unknownStatus = "paused"

        //When
        val releaseStatus = mapReleaseStatusDataToDomain(unknownStatus)

        //Then
        assertEquals(ReleaseStatusDomain.UNKNOWN, releaseStatus)
    }

    @Test
    fun aMissingReleaseStatusBecomesUnknown() {
        //Given
        val missingStatus: String? = null

        //When
        val releaseStatus = mapReleaseStatusDataToDomain(missingStatus)

        //Then
        assertEquals(ReleaseStatusDomain.UNKNOWN, releaseStatus)
    }
}
