package com.alekseivinogradov.anime_list.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_list.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.anime_list.impl.presentation.AnimeListController
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory

class AnimeListFragment : Fragment() {

    private lateinit var binding: FragmentAnimeListBinding

    private val controller: AnimeListController by lazy {
        AnimeListController(
            storeFactory = DefaultStoreFactory(),
            lifecycle = essentyLifecycle()
        )
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
                viewBinding = binding
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }
}