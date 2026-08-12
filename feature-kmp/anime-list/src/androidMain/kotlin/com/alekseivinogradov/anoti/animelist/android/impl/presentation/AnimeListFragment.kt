package com.alekseivinogradov.anoti.animelist.android.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListScreenComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListScreenComponentHolder
import com.alekseivinogradov.anoti.animelist.kmp.R
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.AnimeListController
import com.arkivanov.essenty.lifecycle.essentyLifecycle

class AnimeListFragment : Fragment() {

    private var rootView: View? = null

    private lateinit var screenComponent: AnimeListScreenComponent

    private val controller: AnimeListController by lazy {
        AnimeListController(
            lifecycle = screenComponent.lifecycle,
            mainStore = screenComponent.mainStore,
            animeDatabaseStore = screenComponent.animeDatabaseStore,
            ongoingSectionStore = screenComponent.ongoingSectionStore,
            announcedSectionStore = screenComponent.announcedSectionStore,
            searchSectionStore = screenComponent.searchSectionStore
        )
    }

    override fun onAttach(context: Context) {
        screenComponent = (this.activity as AnimeListScreenComponentHolder).animeListScreenComponent
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
        // rootView is always non-null here: assigned in onCreateView(), cleared only in
        // onDestroyView().
        @Suppress("UnsafeCallOnNullableType")
        val nonNullRootView = rootView!!
        controller.onViewCreated(
            mainView = AnimeListViewImpl(
                rootView = nonNullRootView,
                dateFormatter = screenComponent.dateFormatter,
                viewScope = lifecycleScope,
                coroutineContextProvider = screenComponent.coroutineContextProvider
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
