package com.alekseivinogradov.anime_list.api.domain.store.section_content

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias SectionContentExecutor = CoroutineExecutor<
        SectionContentStore.Intent,
        SectionContentStore.Action,
        SectionContentStore.State,
        SectionContentStore.Message,
        SectionContentStore.Label
        >