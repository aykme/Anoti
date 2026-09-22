package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarExecutor
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore

internal class BottomNavigationBarExecutorImpl : BottomNavigationBarExecutor() {
    override fun executeIntent(intent: BottomNavigationBarStore.Intent) {
        when (intent) {
            is BottomNavigationBarStore.Intent.ChangeSelectedSection -> {
                changeSelectedSection(intent)
            }

            is BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber -> {
                updateFavoritesBadgeNumber(intent)
            }

            BottomNavigationBarStore.Intent.MainSectionClick -> mainSectionClick()
            BottomNavigationBarStore.Intent.FavoritesSectionClick -> favoritesSectionClick()
        }
    }

    private fun changeSelectedSection(
        intent: BottomNavigationBarStore.Intent.ChangeSelectedSection
    ) {
        // The store emits a new state for every message, changed or not. The host resends the
        // current section on every navigation update, so a repeat is dropped here.
        if (state().selectedSection == intent.selectedSection) return

        dispatch(
            BottomNavigationBarStore.Message.ChangeSelectedSection(intent.selectedSection)
        )
    }

    private fun updateFavoritesBadgeNumber(
        intent: BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber
    ) {
        // Same reason: the database resends the badge number on every one of its emissions,
        // and most of those leave it unchanged.
        if (state().favoritesBadgeNumber == intent.favoritesBadgeNumber) return

        dispatch(
            BottomNavigationBarStore.Message.UpdateFavoritesBadgeNumber(intent.favoritesBadgeNumber)
        )
    }

    private fun mainSectionClick() {
        publish(
            BottomNavigationBarStore.Label.NavigateToMain
        )
    }

    private fun favoritesSectionClick() {
        publish(
            BottomNavigationBarStore.Label.NavigateToFavorites
        )
    }
}
