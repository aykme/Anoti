package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias AnnouncedSectionExecutor = CoroutineExecutor<
        AnnouncedSectionStore.Intent,
        AnnouncedSectionStore.Action,
        AnnouncedSectionStore.State,
        AnnouncedSectionStore.Message,
        AnnouncedSectionStore.Label
        >
