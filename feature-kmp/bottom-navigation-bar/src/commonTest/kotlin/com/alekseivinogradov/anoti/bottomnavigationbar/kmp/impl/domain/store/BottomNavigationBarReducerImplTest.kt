package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import kotlin.test.Test
import kotlin.test.assertEquals

private const val BADGE_NUMBER = 7
private const val OTHER_BADGE_NUMBER = 42

class BottomNavigationBarReducerImplTest {

    private val reducer = BottomNavigationBarReducerImpl()

    private val baseState = BottomNavigationBarStore.State(
        selectedSection = SectionDomain.MAIN,
        favoritesBadgeNumber = BADGE_NUMBER
    )

    private fun reduce(msg: BottomNavigationBarStore.Message): BottomNavigationBarStore.State {
        return with(reducer) { baseState.reduce(msg) }
    }

    @Test
    fun changeSelectedSectionReplacesOnlyTheSelectedSection() {
        //Given
        val msg = BottomNavigationBarStore.Message.ChangeSelectedSection(SectionDomain.FAVORITES)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(SectionDomain.FAVORITES, state.selectedSection)
        assertEquals(BADGE_NUMBER, state.favoritesBadgeNumber)
    }

    @Test
    fun updateFavoritesBadgeNumberReplacesOnlyTheBadgeNumber() {
        //Given
        val msg = BottomNavigationBarStore.Message.UpdateFavoritesBadgeNumber(OTHER_BADGE_NUMBER)

        //When
        val state = reduce(msg)

        //Then
        assertEquals(OTHER_BADGE_NUMBER, state.favoritesBadgeNumber)
        assertEquals(SectionDomain.MAIN, state.selectedSection)
    }
}
