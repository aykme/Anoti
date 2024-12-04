package com.alekseivinogradov.anime_list.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.presentation.di.AnimeListComponent
import com.alekseivinogradov.anime_list.impl.presentation.di.DaggerAnimeListComponent
import com.alekseivinogradov.anime_list_platform.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import com.alekseivinogradov.di.api.presentation.main.MainActivityExternal
import com.alekseivinogradov.di.api.presentation.scope.FeatureScope
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import javax.inject.Inject

@FeatureScope
class AnimeListFragment : Fragment() {

    private lateinit var animeListComponent: AnimeListComponent

    private var binding: FragmentAnimeListBinding? = null

    @Inject
    lateinit var coroutineContextProvider: CoroutineContextProvider

    @Inject
    lateinit var mainStore: AnimeListMainStore

    @Inject
    lateinit var animeDatabaseStore: AnimeDatabaseStore

    @Inject
    lateinit var ongoingSectionStore: OngoingSectionStore

    @Inject
    lateinit var announcedSectionStore: AnnouncedSectionStore

    @Inject
    lateinit var searchSectionStore: SearchSectionStore

    @Inject
    lateinit var dateFormatter: DateFormatter

    private val controller: AnimeListController by lazy {
        AnimeListController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore,
            ongoingSectionStore = ongoingSectionStore,
            announcedSectionStore = announcedSectionStore,
            searchSectionStore = searchSectionStore
        )
    }

    override fun onAttach(context: Context) {
        animeListComponent = DaggerAnimeListComponent.factory().create(
            mainComponent = (this.activity as MainActivityExternal).mainComponent
        ).also { it.inject(fragment = this) }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentAnimeListBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(
            mainView = AnimeListViewImpl(
                viewBinding = binding!!,
                dateFormatter = dateFormatter,
                viewScope = lifecycleScope,
                coroutineContextProvider = coroutineContextProvider
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
