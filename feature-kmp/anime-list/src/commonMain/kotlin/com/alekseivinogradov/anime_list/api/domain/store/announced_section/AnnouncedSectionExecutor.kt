package com.alekseivinogradov.anime_list.api.domain.store.announced_section

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias AnnouncedSectionExecutor = CoroutineExecutor<
        AnnouncedSectionStore.Intent,
        AnnouncedSectionStore.Action,
        AnnouncedSectionStore.State,
        AnnouncedSectionStore.Message,
        AnnouncedSectionStore.Label
        >
