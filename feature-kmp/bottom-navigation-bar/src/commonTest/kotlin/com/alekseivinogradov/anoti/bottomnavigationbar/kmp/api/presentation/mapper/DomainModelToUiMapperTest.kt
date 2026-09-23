package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.BottomNavigationBarUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.SectionUi
import kotlin.test.Test
import kotlin.test.assertEquals

private const val BADGE_NUMBER = 3

class DomainModelToUiMapperTest {

    @Test
    fun theMainSectionAndTheBadgeNumberBothReachTheUiModel() {
        //Given
        val state = BottomNavigationBarStore.State(
            selectedSection = SectionDomain.MAIN,
            favoritesBadgeNumber = BADGE_NUMBER
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals(
            BottomNavigationBarUiModel(
                selectedSection = SectionUi.MAIN,
                favoritesBadgeNumber = BADGE_NUMBER
            ),
            uiModel
        )
    }

    @Test
    fun theFavoritesSectionIsMappedToItsOwnUiSection() {
        //Given
        val state = BottomNavigationBarStore.State(selectedSection = SectionDomain.FAVORITES)

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals(SectionUi.FAVORITES, uiModel.selectedSection)
        assertEquals(0, uiModel.favoritesBadgeNumber)
    }
}
