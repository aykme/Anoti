package com.alekseivinogradov.anime_favorites.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.api.domain.ToastProvider
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anime_favorites.impl.data.source.AnimeFavoritesSourceImpl
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.toast.AnotiToast
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory

class AnimeFavoritesFragment : Fragment() {

    private var binding: FragmentAnimeFavoritesBinding? = null

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val animeFavoritesSource: AnimeFavoritesSource = AnimeFavoritesSourceImpl(
        service = shikimoriService,
        safeApi = SafeApiImpl
    )

    private val fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(
        source = animeFavoritesSource
    )

    private val animeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(requireContext())
    }

    private val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            storeFactory = DefaultStoreFactory(),
            lifecycle = essentyLifecycle(),
            favoritesUsecases = getFavoritesUsecases(),
            toastProvider = getToastProvider(requireContext()),
            databaseRepository = animeDatabaseRepository
        )
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
                dateFormatter = DateFormatter.getInstance(view.context)
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    private fun getFavoritesUsecases() = FavoritesUsecases(
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
    )

    private fun getToastProvider(context: Context) = ToastProvider(
        makeConnectionErrorToast = { AnotiToast.makeConnectionErrorToast(context) },
        makeUnknownErrorToast = { AnotiToast.makeUnknownErrorToast(context) }
    )
}
