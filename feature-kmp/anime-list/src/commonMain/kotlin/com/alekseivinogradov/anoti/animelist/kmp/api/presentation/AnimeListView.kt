package com.alekseivinogradov.anoti.animelist.kmp.api.presentation

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.AnimeListUiModel
import com.arkivanov.mvikotlin.core.view.MviView

/**
 * The view contract [com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.AnimeListRoute]
 * implements to render the main store's state.
 */
interface AnimeListView : MviView<AnimeListUiModel, AnimeListMainStore.Intent>
