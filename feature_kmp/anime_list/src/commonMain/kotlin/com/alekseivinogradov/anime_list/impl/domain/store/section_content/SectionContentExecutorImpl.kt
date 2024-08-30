package com.alekseivinogradov.anime_list.impl.domain.store.section_content

import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentExecutor
import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore

internal class SectionContentExecutorImpl : SectionContentExecutor() {
    override fun executeAction(action: SectionContentStore.Action) {
        super.executeAction(action)
    }

    override fun executeIntent(intent: SectionContentStore.Intent) {
        super.executeIntent(intent)
    }
}