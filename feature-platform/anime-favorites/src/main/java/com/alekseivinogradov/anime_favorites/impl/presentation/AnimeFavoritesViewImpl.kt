package com.alekseivinogradov.anime_favorites.impl.presentation

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_base.impl.presentation.adapter.decorator.BottomSpaceLastItemDecorator
import com.alekseivinogradov.anime_base.impl.presentation.adapter.decorator.EdgeToEdgeItemDecorator
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anime_favorites.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.UiModel
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_favorites.impl.presentation.adapter.AnimeFavoritesAdapter
import com.alekseivinogradov.anime_favorites_platform.R
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.edge_to_edge.isEdgeToEdgeEnabled
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

    private var itemDecorator: EdgeToEdgeItemDecorator? = null

    init {
        initEdgeToEdgeListenerIfNeeded()
        initSwipeToRefresh()
        initCommonFields()
        initRv()
    }

    override val renderer: ViewRenderer<UiModel> = diff {
        diff(
            get = ::getContentType,
            set = ::setContentType
        )
        diff(
            get = ::getListItems,
            set = ::setListItems
        )
    }

    private fun initEdgeToEdgeListenerIfNeeded() {
        if (isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root)
            { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                val animeFavoritesEmptyLayout =
                    viewBinding.animeFavoritesEmptyContainer.animeFavoritesEmptyLayout
                animeFavoritesEmptyLayout.setPadding(
                    /* left = */animeFavoritesEmptyLayout.paddingLeft,
                    /* top = */systemBars.top,
                    /* right = */animeFavoritesEmptyLayout.paddingRight,
                    /* bottom = */animeFavoritesEmptyLayout.paddingBottom
                )

                itemDecorator?.let { oldItemDecorator: EdgeToEdgeItemDecorator ->
                    viewBinding.animeFavoritesRv.removeItemDecoration(oldItemDecorator)
                }
                itemDecorator = EdgeToEdgeItemDecorator(systemBarTopOffset = systemBars.top)
                itemDecorator?.let { newItemDecorator: EdgeToEdgeItemDecorator ->
                    viewBinding.animeFavoritesRv.addItemDecoration(newItemDecorator)
                }

                insets
            }
        }
    }

    private fun initSwipeToRefresh() {
        with(viewBinding) {
            swipeRefreshLayout.setProgressViewOffset(
                /* scale = */ false,
                /* start = */ com.alekseivinogradov.celebrity.api.domain.SWIPE_REFRESH_START_OFFSET,
                /* end = */ com.alekseivinogradov.celebrity.api.domain.SWIPE_REFRESH_END_OFFSET
            )
            swipeRefreshLayout.setColorSchemeResources(res_R.color.cinnabar_500)
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
            animeFavoritesEmptyContainer.mainImage.contentDescription = context
                .getString(R.string.empty_list_image_description)
            animeFavoritesEmptyContainer.mainInfoText.text = context.getString(R.string.empty_list)
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
            animeFavoritesRv.addItemDecoration(BottomSpaceLastItemDecorator())
        }
    }

    private fun getContentType(uiModel: UiModel): ContentTypeUi {
        return uiModel.contentType
    }

    private fun setContentType(contentType: ContentTypeUi) {
        with(viewBinding) {
            when (contentType) {
                ContentTypeUi.LOADED -> {
                    animeFavoritesEmptyContainer.animeFavoritesEmptyLayout.isVisible = false
                    connectionStatusImage.isVisible = false
                    swipeRefreshLayout.isEnabled = true
                    animeFavoritesRv.isVisible = true
                }

                ContentTypeUi.LOADING -> {
                    animeFavoritesEmptyContainer.animeFavoritesEmptyLayout.isVisible = false
                    animeFavoritesRv.isVisible = false
                    swipeRefreshLayout.isEnabled = false
                    connectionStatusImage.isVisible = true
                }

                ContentTypeUi.EMPTY -> {
                    animeFavoritesRv.isVisible = false
                    swipeRefreshLayout.isEnabled = false
                    connectionStatusImage.isVisible = false
                    animeFavoritesEmptyContainer.animeFavoritesEmptyLayout.isVisible = true
                }
            }
        }
    }

    private fun getListItems(uiModel: UiModel): List<ListItemUi> {
        return uiModel.listItems
    }

    private fun setListItems(listItems: List<ListItemUi>) {
        adapter.submitList(listItems) {
            if (listItems.isNotEmpty()) {
                dispatch(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
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
