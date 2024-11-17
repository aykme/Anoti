package com.alekseivinogradov.anime_favorites.impl.presentation

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anime_favorites.api.presentation.model.UiModel
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_favorites.impl.presentation.adapter.AnimeFavoritesAdapter
import com.alekseivinogradov.anime_favorites_platform.R
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatter
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.alekseivinogradov.res.R as res_R

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
        initSwipeToRefresh()
        initCommonFields()
        initRv()
    }

    override val renderer: ViewRenderer<UiModel> = diff {
        diff(
            get = ::getListItems,
            set = ::setListItems
        )
    }

    private fun initSwipeToRefresh() {
        with(viewBinding) {
            swipeRefreshLayout.setProgressViewOffset(
                /* scale = */ false,
                /* start = */ com.alekseivinogradov.celebrity.api.domain.SWIPE_REFRESH_START_OFFSET,
                /* end = */ com.alekseivinogradov.celebrity.api.domain.SWIPE_REFRESH_END_OFFSET
            )
            swipeRefreshLayout.setColorSchemeResources(res_R.color.pink)
            swipeRefreshLayout.setOnRefreshListener {
                dispatch(AnimeFavoritesMainStore.Intent.UpdateSection)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun initCommonFields() {
        with(viewBinding) {
            swipeRefreshLayout.isVisible = true
            animeFavoritesLayout.isVisible = true
            animeFavoritesEmptyLayout.mainImage.contentDescription = context
                .getString(R.string.empty_list_image_description)
            animeFavoritesEmptyLayout.mainInfoText.text = context.getString(R.string.empty_list)
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
        }
    }

    private fun getListItems(uiModel: UiModel): List<ListItemUi> {
        return uiModel.listItems
    }

    private fun setListItems(listItems: List<ListItemUi>) {
        adapter.submitList(listItems)

        with(viewBinding) {
            if (listItems.isNotEmpty()) {
                animeFavoritesEmptyLayout.animeFavoritesEmptyLayout.isVisible = false
                swipeRefreshLayout.isEnabled = true
                animeFavoritesRv.isVisible = true
            } else {
                swipeRefreshLayout.isEnabled = false
                animeFavoritesRv.isVisible = false
                animeFavoritesEmptyLayout.animeFavoritesEmptyLayout.isVisible = true
            }
        }
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
