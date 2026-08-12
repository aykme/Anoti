package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesComponentFactoryHolder
import com.alekseivinogradov.anoti.animefavorites.kmp.R
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.essenty.lifecycle.essentyLifecycle

class AnimeFavoritesFragment : Fragment() {

    private var rootView: View? = null

    private lateinit var mainStore: AnimeFavoritesMainStore

    private lateinit var animeDatabaseStore: AnimeDatabaseStore

    private lateinit var dateFormatter: DateFormatter

    private lateinit var coroutineContextProvider: CoroutineContextProvider

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        )
    }

    override fun onAttach(context: Context) {
        val animeFavoritesComponent = (this.activity as AnimeFavoritesComponentFactoryHolder)
            .animeFavoritesComponentFactory
            .createAnimeFavoritesComponent()
        coroutineContextProvider = animeFavoritesComponent.coroutineContextProvider
        dateFormatter = animeFavoritesComponent.dateFormatter
        animeDatabaseStore = animeFavoritesComponent.animeDatabaseStore
        mainStore = animeFavoritesComponent.mainStore
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater
        .inflate(R.layout.fragment_anime_favorites, container, false)
        .also { rootView = it }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(
            mainView = AnimeFavoritesViewImpl(
                rootView = rootView!!,
                dateFormatter = dateFormatter,
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
