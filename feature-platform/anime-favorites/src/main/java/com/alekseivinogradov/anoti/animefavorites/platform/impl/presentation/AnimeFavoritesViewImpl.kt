package com.alekseivinogradov.anoti.animefavorites.platform.impl.presentation

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anoti.animebase.platform.impl.presentation.adapter.decorator.BottomSpaceLastItemDecorator
import com.alekseivinogradov.anoti.animebase.platform.impl.presentation.adapter.decorator.EdgeToEdgeItemDecorator
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.platform.impl.presentation.adapter.AnimeFavoritesAdapter
import com.alekseivinogradov.anoti.animefavorites.platform.R
import com.alekseivinogradov.anoti.animefavorites.platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.platform.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.res.R as res_R
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
                /* start = */ com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SWIPE_REFRESH_START_OFFSET,
                /* end = */ com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SWIPE_REFRESH_END_OFFSET
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
