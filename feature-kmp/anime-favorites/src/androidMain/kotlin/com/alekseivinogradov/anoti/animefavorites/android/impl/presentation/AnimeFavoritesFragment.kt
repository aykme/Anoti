package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesComponent
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.DaggerAnimeFavoritesComponent
import com.alekseivinogradov.anoti.animefavorites.kmp.R
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.di.platform.api.presentation.main.MainActivityExternal
import com.alekseivinogradov.anoti.di.platform.api.presentation.scope.FeatureScope
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import javax.inject.Inject

@FeatureScope
class AnimeFavoritesFragment : Fragment() {

    private lateinit var animeFavoritesComponent: AnimeFavoritesComponent

    private var rootView: View? = null

    @Inject
    internal lateinit var mainStore: AnimeFavoritesMainStore

    @Inject
    lateinit var animeDatabaseStore: AnimeDatabaseStore

    @Inject
    lateinit var dateFormatter: DateFormatter

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        )
    }

    override fun onAttach(context: Context) {
        animeFavoritesComponent = DaggerAnimeFavoritesComponent.factory().create(
            mainComponent = (this.activity as MainActivityExternal).mainComponent
        ).also { it.inject(fragment = this) }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_anime_favorites, container, false)
        .also { rootView = it }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(
            mainView = AnimeFavoritesViewImpl(
                rootView = rootView!!,
                dateFormatter = dateFormatter
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
