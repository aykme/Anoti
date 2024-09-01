package com.alekseivinogradov.anime_list.impl.domain.store.section_content

import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentExecutor
import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.alekseivinogradov.anime_list.api.domain.usecase.FetchAnimeListUsecase

internal class SectionContentExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeListUsecase
) : SectionContentExecutor() {
    override fun executeAction(action: SectionContentStore.Action) {
        super.executeAction(action)
    }

    override fun executeIntent(intent: SectionContentStore.Intent) {
        super.executeIntent(intent)
    }
}