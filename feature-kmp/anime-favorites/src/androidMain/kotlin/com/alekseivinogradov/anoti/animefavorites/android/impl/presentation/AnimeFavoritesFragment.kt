package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesScreenComponentHolder
import com.alekseivinogradov.anoti.animefavorites.kmp.R
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.arkivanov.essenty.lifecycle.essentyLifecycle

class AnimeFavoritesFragment : Fragment() {

    private var rootView: View? = null

    private lateinit var screenComponent: AnimeFavoritesScreenComponent

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = screenComponent.lifecycle,
            mainStore = screenComponent.mainStore,
            animeDatabaseStore = screenComponent.animeDatabaseStore
        )
    }

    override fun onAttach(context: Context) {
        screenComponent =
            (this.activity as AnimeFavoritesScreenComponentHolder).animeFavoritesScreenComponent
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
        // rootView is always non-null here: assigned in onCreateView(), cleared only in
        // onDestroyView().
        @Suppress("UnsafeCallOnNullableType")
        val nonNullRootView = rootView!!
        controller.onViewCreated(
            mainView = AnimeFavoritesViewImpl(
                rootView = nonNullRootView,
                dateFormatter = screenComponent.dateFormatter,
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
