package com.alekseivinogradov.anime_favorites.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.impl.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.database.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.database.impl.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl

class AnimeFavoritesFragment : Fragment() {

    private val tag = "AnimeFavorites"

    private var binding: FragmentAnimeFavoritesBinding? = null


    private val animeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(requireContext())
    }

    private val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    private val fetchAllAnimeDatabaseItemsFlowUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                FetchAllAnimeDatabaseItemsFlowUsecase(repository = animeDatabaseRepository)
            }

    private val insertAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                InsertAnimeDatabaseItemUsecase(repository = animeDatabaseRepository)
            }

    private val deleteAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                DeleteAnimeDatabaseItemUsecase(repository = animeDatabaseRepository)
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
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}