package com.alekseivinogradov.anime_list.api.domain.store.search_section

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias SearchSectionExecutor = CoroutineExecutor<
        SearchSectionStore.Intent,
        SearchSectionStore.Action,
        SearchSectionStore.State,
        SearchSectionStore.Message,
        SearchSectionStore.Label>