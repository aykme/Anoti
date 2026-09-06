package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesExecutor
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// One function per Intent handled, not incidental growth.
@Suppress("TooManyFunctions")
class AnimeFavoritesExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: FavoritesUsecases,
    private var toastProvider: ToastProvider
) : AnimeFavoritesExecutor() {

    private var updateListItemsJob: Job? = null
    private var updateSectionJob: Job? = null
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeIntent(intent: AnimeFavoritesMainStore.Intent) {
        when (intent) {
            is AnimeFavoritesMainStore.Intent.UpdateListItems -> updateListItems(intent)
            is AnimeFavoritesMainStore.Intent.ItemsSubmittedToList -> itemsSubmittedToList()
            AnimeFavoritesMainStore.Intent.OpenSection -> openSection()
            AnimeFavoritesMainStore.Intent.UpdateSection -> updateSection()
            AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground -> {
                updateAllItemsInBackground()
            }

            is AnimeFavoritesMainStore.Intent.ItemClick -> itemClick(intent)
            is AnimeFavoritesMainStore.Intent.InfoTypeClick -> infoTypeClick(intent)
            is AnimeFavoritesMainStore.Intent.NotificationClick -> notificationClick(intent)
            is AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick -> {
                episodesViewedMinusClick(intent)
            }

            is AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick -> {
                episodesViewedPlusClick(intent)
            }
        }
    }

    private fun updateListItems(intent: AnimeFavoritesMainStore.Intent.UpdateListItems) {
        updateListItemsJob?.cancel()
        updateListItemsJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            dispatch(AnimeFavoritesMainStore.Message.UpdateListItems(intent.listItems))
            if (intent.listItems.isEmpty() && state().contentType != ContentTypeDomain.EMPTY) {
                dispatch(
                    AnimeFavoritesMainStore.Message.ChangeContentType(
                        ContentTypeDomain.LOADING()
                    )
                )
                delay(ANIMATION_DURATION_SHORT)
                dispatch(
                    AnimeFavoritesMainStore.Message.ChangeContentType(
                        ContentTypeDomain.EMPTY
                    )
                )
            }
        }
    }

    private fun itemsSubmittedToList() {
        val contentType = state().contentType
        if (contentType is ContentTypeDomain.LOADING && contentType.hasMinimumDuration) return
        if (contentType != ContentTypeDomain.LOADED) {
            dispatch(AnimeFavoritesMainStore.Message.ChangeContentType(ContentTypeDomain.LOADED))
        }
    }

    // The section became selected: not a data refresh, just resets any leftover extra-info
    // display state so it doesn't survive from before, on the same minimum-duration timer as
    // updateSection() so opening the screen doesn't flash faster than a manual refresh does.
    private fun openSection() {
        updateListItemsJob?.cancel()
        dispatch(
            AnimeFavoritesMainStore.Message.ChangeContentType(
                ContentTypeDomain.LOADING(hasMinimumDuration = true)
            )
        )
        publish(AnimeFavoritesMainStore.Label.ResetExtraInfo)
        resolveContentTypeAfterMinimumDuration()
    }

    private fun updateSection() {
        // A pending empty-list transition from a stale updateListItems() call must not be left
        // to fire later and unconditionally overwrite this refresh's own resolution.
        updateListItemsJob?.cancel()
        dispatch(
            AnimeFavoritesMainStore.Message.ChangeContentType(
                ContentTypeDomain.LOADING(hasMinimumDuration = true)
            )
        )
        publish(AnimeFavoritesMainStore.Label.ResetExtraInfo)
        publish(AnimeFavoritesMainStore.Label.UpdateSection)
        resolveContentTypeAfterMinimumDuration()
    }

    private fun resolveContentTypeAfterMinimumDuration() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            delay(ANIMATION_DURATION_SHORT)
            // Only resolve if nothing else already has (e.g. updateListItems()'s own
            // LOADING -> EMPTY transition, if the refreshed list turned out empty).
            val contentType = state().contentType
            if (contentType is ContentTypeDomain.LOADING && contentType.hasMinimumDuration) {
                val finalContentType = if (state().listItems.isEmpty()) {
                    ContentTypeDomain.EMPTY
                } else {
                    ContentTypeDomain.LOADED
                }
                dispatch(AnimeFavoritesMainStore.Message.ChangeContentType(finalContentType))
            }
        }
    }

    private fun updateAllItemsInBackground() {
        usecases.updateAllAnimeInBackgroundOnceUsecase.execute()
    }

    private fun itemClick(intent: AnimeFavoritesMainStore.Intent.ItemClick) {
        publish(AnimeFavoritesMainStore.Label.ItemClick(intent.id))
    }

    private fun infoTypeClick(intent: AnimeFavoritesMainStore.Intent.InfoTypeClick) {
        val listItem = state().listItems.find { listItemDomain: ListItemDomain ->
            listItemDomain.id == intent.id
        } ?: return

        if (listItem.isExtraInfoEnabled) {
            changeInfoTypeToMain(listItem)
        } else {
            changeInfoTypeToExtra(listItem)
        }
    }

    private fun notificationClick(intent: AnimeFavoritesMainStore.Intent.NotificationClick) {
        publish(AnimeFavoritesMainStore.Label.DisableNotificationClick(intent.id))
    }

    private fun episodesViewedMinusClick(
        intent: AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick
    ) {
        val listItem = state().listItems
            .find { listItemDomain: ListItemDomain ->
                listItemDomain.id == intent.id
            } ?: return

        if (listItem.episodesViewed <= 0) return

        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(
                    episodesViewed = listItem.episodesViewed - 1
                )
            )
        )
    }

    private fun episodesViewedPlusClick(
        intent: AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick
    ) {
        val listItem = state().listItems
            .find { listItemDomain: ListItemDomain ->
                listItemDomain.id == intent.id
            } ?: return

        if (listItem.episodesViewed >= getMaxEpisodesViewedNumber(listItem)) return

        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(
                    episodesViewed = listItem.episodesViewed + 1
                )
            )
        )
    }

    private fun changeInfoTypeToMain(listItem: ListItemDomain) {
        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(isExtraInfoEnabled = false)
            )
        )
    }

    private fun changeInfoTypeToExtra(listItem: ListItemDomain) {
        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(isExtraInfoEnabled = true)
            )
        )

        val isOngoingStatus = listItem.releaseStatus == ReleaseStatusDomain.ONGOING

        if (isOngoingStatus && listItem.nextEpisodeAt == null) {
            updateAnimeDetails(listItem.id)
        }
    }

    private fun updateAnimeDetails(id: AnimeId) {
        updateAnimeDetailsJobMap[id]?.cancel()
        updateAnimeDetailsJobMap[id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                val result = usecases
                    .fetchAnimeDetailsByIdUsecase
                    .execute(id)

                when (result) {
                    is CallResult.Success -> onSuccessUpdateAnimeDetails(
                        currentItemId = id,
                        updateListItem = result.value
                    )

                    is CallResult.HttpError,
                    is CallResult.NetworkError -> toastProvider.makeConnectionErrorToast()

                    is CallResult.OtherError -> toastProvider.makeUnknownErrorToast()
                }
            }
    }

    private fun onSuccessUpdateAnimeDetails(
        currentItemId: AnimeId,
        updateListItem: ListItemDomain
    ) {
        val currentListItem = state().listItems.find { listItemDomain: ListItemDomain ->
            listItemDomain.id == currentItemId
        } ?: return

        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = currentListItem.copy(
                    nextEpisodeAt = updateListItem.nextEpisodeAt
                )
            )
        )
    }

    private fun getMaxEpisodesViewedNumber(listItem: ListItemDomain): Int {
        return when (listItem.releaseStatus) {
            ReleaseStatusDomain.ONGOING -> listItem.episodesAired ?: 0
            ReleaseStatusDomain.ANNOUNCED -> 0
            ReleaseStatusDomain.RELEASED -> listItem.episodesTotal ?: 0
            ReleaseStatusDomain.UNKNOWN -> 0
        }
    }
}
