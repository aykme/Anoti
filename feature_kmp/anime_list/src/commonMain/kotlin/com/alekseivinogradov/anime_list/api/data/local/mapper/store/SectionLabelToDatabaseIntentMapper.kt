package com.alekseivinogradov.anime_list.api.data.local.mapper.store

import com.alekseivinogradov.anime_list.api.data.local.mapper.toDb
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapOngoingSectionLabelToDatabaseIntent(label: OngoingSectionStore.Label)
        : DatabaseStore.Intent {
    return when (label) {
        is OngoingSectionStore.Label.EnableNotification -> {
            DatabaseStore.Intent.InsertAnimeDatabaseItem(label.listItem.toDb())
        }

        is OngoingSectionStore.Label.DisableNotification -> {
            DatabaseStore.Intent.DeleteAnimeDatabaseItem(label.id)
        }
    }
}