package com.alekseivinogradov.anime_list.impl.domain.store.section_content

import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentExecutor
import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.alekseivinogradov.anime_list.api.domain.usecase.FetchAnimeListUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private var updateSectionJob: Job? = null

internal class SectionContentExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeListUsecase
) : SectionContentExecutor() {

    override fun executeIntent(intent: SectionContentStore.Intent) {
        when (intent) {
            SectionContentStore.Intent.InitSection -> initSection()
            SectionContentStore.Intent.UpdateSection -> updateSection()
            is SectionContentStore.Intent.EpisodesInfoClick -> episodeInfoClick()
            is SectionContentStore.Intent.NotificationClick -> notificationClick()
        }
    }

    private fun initSection() {
        if (state().listItems.isEmpty()) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch {
            dispatch(
                SectionContentStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = fetchAnimeListUsecase.execute(
                page = 1,
                itemsPerPage = ITEMS_PER_PAGE
            )
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        SectionContentStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            SectionContentStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                        )
                    } else {
                        dispatch(
                            SectionContentStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    SectionContentStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
                )
            }
        }
    }

    private fun episodeInfoClick() {

    }

    private fun notificationClick() {

    }
}