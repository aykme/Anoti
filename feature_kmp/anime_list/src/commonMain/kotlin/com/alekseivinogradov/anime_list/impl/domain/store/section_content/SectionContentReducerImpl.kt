package com.alekseivinogradov.anime_list.impl.domain.store.section_content

import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class SectionContentReducerImpl :
    Reducer<SectionContentStore.State, SectionContentStore.Message> {

    override fun SectionContentStore.State.reduce(msg: SectionContentStore.Message):
            SectionContentStore.State {
        return when (msg) {
            is SectionContentStore.Message.ChangeContentType -> copy(
                contentType = msg.contentType
            )

            is SectionContentStore.Message.UpdateListItems -> copy(
                listItems = msg.listItems
            )
        }
    }
}