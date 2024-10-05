package com.alekseivinogradov.bottom_navigation_bar.impl.domain.store

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class BottomNavigationBarReducerImpl :
    Reducer<BottomNavigationBarStore.State, BottomNavigationBarStore.Message> {

    override fun BottomNavigationBarStore.State.reduce(
        msg: BottomNavigationBarStore.Message
    ): BottomNavigationBarStore.State {
        return when (msg) {
            is BottomNavigationBarStore.Message.ChangeSelectedSection -> copy(
                selectedSection = msg.selectedSection
            )

            is BottomNavigationBarStore.Message.UpdateFavoritesBadgeNumber -> copy(
                favoritesBadgeNumber = msg.favoritesBadgeNumber
            )
        }
    }
}
