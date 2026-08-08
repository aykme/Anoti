package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
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
