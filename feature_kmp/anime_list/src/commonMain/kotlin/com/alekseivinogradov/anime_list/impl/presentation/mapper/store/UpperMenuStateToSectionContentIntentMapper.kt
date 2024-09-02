package com.alekseivinogradov.anime_list.impl.presentation.mapper.store

import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SectionDomain
import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore

internal fun mapUpperMenuStateToOngoingSectionContentIntent(state: UpperMenuStore.State):
        SectionContentStore.Intent? {
    return when (state.selectedSection) {
        SectionDomain.ONGOINGS -> SectionContentStore.Intent.InitSection
        SectionDomain.ANNOUNCED,
        SectionDomain.SEARCH -> null
    }
}