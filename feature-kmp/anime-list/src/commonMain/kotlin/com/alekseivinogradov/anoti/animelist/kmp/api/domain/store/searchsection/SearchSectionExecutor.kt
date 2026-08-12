package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias SearchSectionExecutor = CoroutineExecutor<
    SearchSectionStore.Intent,
    SearchSectionStore.Action,
    SearchSectionStore.State,
    SearchSectionStore.Message,
    SearchSectionStore.Label
    >
