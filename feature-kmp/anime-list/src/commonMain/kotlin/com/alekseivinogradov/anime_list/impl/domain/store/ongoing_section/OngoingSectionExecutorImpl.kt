package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.AnimeDetails
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.FIRST_PAGE
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.paging.PageLoadResult
import com.alekseivinogradov.celebrity.api.domain.paging.Paginator
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class OngoingSectionExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: OngoingUsecases,
    private val toastProvider: ToastProvider
) : OngoingSectionExecutor() {

    private var updateSectionJob: Job? = null
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private var paginator: Paginator<ListItemDomain> = createPaginator()

    override fun executeAction(action: OngoingSectionStore.Action) {
        when (action) {
            OngoingSectionStore.Action.InitSection -> openSection()
        }
    }

    override fun executeIntent(intent: OngoingSectionStore.Intent) {
        when (intent) {
            OngoingSectionStore.Intent.OpenSection -> openSection()
            OngoingSectionStore.Intent.UpdateSection -> updateSection()
            OngoingSectionStore.Intent.LoadNextPage -> loadNextPage()
            is OngoingSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun createPaginator(): Paginator<ListItemDomain> {
        return Paginator(
            firstPage = FIRST_PAGE,
            loadPage = { page -> usecases.fetchOngoingAnimeListUsecase.execute(page = page) }
        )
    }

    private fun openSection() {
        publish(OngoingSectionStore.Label.ResetListPositionAfterUpdate)
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        paginator = createPaginator()
        updateSectionJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            dispatch(
                OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            dispatch(
                OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoIds = setOf()
                )
            )
            dispatch(
                OngoingSectionStore.Message.UpdateAnimeDetails(
                    animeDetails = AnimeDetails()
                )
            )
            when (val result = paginator.loadFirstPage()) {
                is PageLoadResult.Success -> {
                    dispatch(OngoingSectionStore.Message.UpdateListItems(result.items))
                    dispatch(
                        OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                    )
                }

                is PageLoadResult.Error -> {
                    toastProvider.makeConnectionErrorToast()
                    dispatch(
                        OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }

                is PageLoadResult.UnexpectedError -> {
                    toastProvider.makeUnknownErrorToast()
                    dispatch(
                        OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        scope.launch(coroutineContextProvider.mainCoroutineContext) {
            when (val result = paginator.loadNextPage()) {
                is PageLoadResult.Success -> dispatch(
                    OngoingSectionStore.Message.UpdateListItems(
                        state().sectionContent.listItems + result.items
                    )
                )

                is PageLoadResult.Error -> toastProvider.makeConnectionErrorToast()
                is PageLoadResult.UnexpectedError -> toastProvider.makeUnknownErrorToast()
                null -> Unit
            }
        }
    }

    private fun episodeInfoClick(intent: OngoingSectionStore.Intent.EpisodesInfoClick) {
        val listItem = state().sectionContent.listItems.find { it.id == intent.id } ?: return
        if (state().sectionContent.enabledExtraEpisodesInfoIds.contains(listItem.id)) {
            availableEpisodesInfoClick(listItem)
        } else {
            extraEpisodesInfoClick(listItem)
        }
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = state()
            .sectionContent
            .enabledExtraEpisodesInfoIds
            .toMutableSet().apply {
                remove(listItem.id)
            }.toSet()

        dispatch(
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = state()
            .sectionContent
            .enabledExtraEpisodesInfoIds
            .toMutableSet().apply {
                add(listItem.id)
            }.toSet()

        dispatch(
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
        if (
            listItem.releaseStatus == ReleaseStatusDomain.ONGOING &&
            !state().sectionContent.animeDetails.nextEpisodesInfo.contains(listItem.id)
        ) {
            updateAnimeDetails(listItem.id)
        }
    }

    private fun updateAnimeDetails(id: AnimeId) {
        updateAnimeDetailsJobMap[id]?.cancel()
        updateAnimeDetailsJobMap[id] = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            val result = usecases
                .fetchAnimeDetailsByIdUsecase
                .execute(id)

            when (result) {
                is CallResult.Success -> onSuccessUpdateAnimeDetails(
                    updateListItem = result.value
                )

                is CallResult.HttpError,
                is CallResult.OtherError -> toastProvider.makeConnectionErrorToast()
            }
        }
    }

    private fun onSuccessUpdateAnimeDetails(
        updateListItem: ListItemDomain
    ) {
        val newNextEpisodesInfo = state()
            .sectionContent
            .animeDetails
            .nextEpisodesInfo
            .toMutableMap().apply {
                this[updateListItem.id] = updateListItem.nextEpisodeAt
            }

        dispatch(
            OngoingSectionStore.Message.UpdateAnimeDetails(
                animeDetails = state().sectionContent.animeDetails.copy(
                    nextEpisodesInfo = newNextEpisodesInfo
                )
            )
        )
    }
}
