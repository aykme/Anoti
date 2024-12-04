package com.alekseivinogradov.anime_favorites.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.impl.presentation.di.AnimeFavoritesComponent
import com.alekseivinogradov.anime_favorites.impl.presentation.di.DaggerAnimeFavoritesComponent
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import com.alekseivinogradov.di.api.presentation.main.MainActivityExternal
import com.alekseivinogradov.di.api.presentation.scope.FeatureScope
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import javax.inject.Inject

@FeatureScope
class AnimeFavoritesFragment : Fragment() {

    private lateinit var animeFavoritesComponent: AnimeFavoritesComponent

    private var binding: FragmentAnimeFavoritesBinding? = null

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
    ) = FragmentAnimeFavoritesBinding
        .inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(
            mainView = AnimeFavoritesViewImpl(
                viewBinding = binding!!,
                dateFormatter = dateFormatter
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
