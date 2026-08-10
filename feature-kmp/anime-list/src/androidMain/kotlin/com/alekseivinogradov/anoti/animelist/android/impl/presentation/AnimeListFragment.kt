package com.alekseivinogradov.anoti.animelist.android.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.DaggerAnimeListComponent
import com.alekseivinogradov.anoti.animelist.kmp.R
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.AnimeListController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.scope.FeatureScope
import com.alekseivinogradov.anoti.di.android.api.presentation.main.MainActivityExternal
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import javax.inject.Inject

@FeatureScope
class AnimeListFragment : Fragment() {

    private lateinit var animeListComponent: AnimeListComponent

    private var rootView: View? = null

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
    ): View = inflater.inflate(R.layout.fragment_anime_list, container, false)
        .also { rootView = it }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(
            mainView = AnimeListViewImpl(
                rootView = rootView!!,
                dateFormatter = dateFormatter,
                viewScope = lifecycleScope,
                coroutineContextProvider = coroutineContextProvider
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
