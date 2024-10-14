package com.alekseivinogradov.anime_favorites.impl.presentation

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_base.api.domain.SWIPE_REFRESH_END_OFFSET
import com.alekseivinogradov.anime_base.api.domain.SWIPE_REFRESH_START_OFFSET
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anime_favorites.api.presentation.model.UiModel
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_favorites.impl.presentation.adapter.AnimeFavoritesAdapter
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.date.formatter.DateFormatter
import com.alekseivinogradov.theme.R
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer

internal class AnimeFavoritesViewImpl(
    private val viewBinding: FragmentAnimeFavoritesBinding,
    dateFormatter: DateFormatter
) : AnimeFavoritesView, BaseMviView<UiModel, AnimeFavoritesMainStore.Intent>() {

    private val context
        get() = viewBinding.root.context

    private val adapter = AnimeFavoritesAdapter(
        itemClickAdapterCallback = ::itemClickAdapterCallback,
        infoTypeClickAdapterCallback = ::infoTypeClickAdapterCallback,
        notificationClickAdapterCallback = ::notificationClickAdapterCallback,
        episodesViewedMinusClickAdapterCallback = ::episodesViewedMinusClickAdapterCallback,
        episodesViewedPlusClickAdapterCallback = ::episodesViewedPlusClickAdapterCallback,
        dateFormatter = dateFormatter
    )

    init {
        setSwipeToRefresh()
        setCommonFields()
        initRv()
    }

    override val renderer: ViewRenderer<UiModel> = diff {
        diff(
            get = ::getListItems,
            set = ::setListItems
        )
    }

    private fun setSwipeToRefresh() {
        with(viewBinding) {
            swipeRefreshLayout.setProgressViewOffset(
                /* scale = */ false,
                /* start = */ SWIPE_REFRESH_START_OFFSET,
                /* end = */ SWIPE_REFRESH_END_OFFSET
            )
            swipeRefreshLayout.setColorSchemeResources(R.color.pink)
            swipeRefreshLayout.setOnRefreshListener {
                dispatch(AnimeFavoritesMainStore.Intent.UpdateSection)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setCommonFields() {
        with(viewBinding) {
            swipeRefreshLayout.isVisible = true
            animeFavoritesLayout.isVisible = true
            animeFavoritesRv.isVisible = true
        }
    }

    private fun initRv() {
        with(viewBinding) {
            animeFavoritesRv.adapter = adapter
            animeFavoritesRv.layoutManager = LinearLayoutManager(
                /* context = */ context,
                /* orientation = */ LinearLayoutManager.VERTICAL,
                /* reverseLayout = */ false
            )
            animeFavoritesRv.itemAnimator = null
        }
    }

    private fun getListItems(uiModel: UiModel): List<ListItemUi> {
        return uiModel.listItems
    }

    private fun setListItems(listItems: List<ListItemUi>) {
        adapter.submitList(listItems)
    }

    private fun itemClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.ItemClick(id))
    }

    private fun infoTypeClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.InfoTypeClick(id))
    }

    private fun notificationClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.NotificationClick(id))
    }

    private fun episodesViewedMinusClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick(id))
    }

    private fun episodesViewedPlusClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id))
    }
}
