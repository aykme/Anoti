package com.alekseivinogradov.anime_list.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.anime_list.impl.data.remote.source.AnimeListSourceImpl
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeAnnouncedListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.Usecases
import com.alekseivinogradov.anime_list.impl.presentation.AnimeListController
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiService
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_network_base.impl.remote.ShikimoriApiServiceImpl
import com.alekseivinogradov.network.api.data.remote.SafeApi
import com.alekseivinogradov.network.impl.data.remote.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory

class AnimeListFragment : Fragment() {

    private lateinit var binding: FragmentAnimeListBinding

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val safeApi: SafeApi = SafeApiImpl()

    private val animeListSource: AnimeListSource = AnimeListSourceImpl(
        service = shikimoriService,
        safeApi = safeApi
    )

    private val fetchAnimeOngoingListUsecase =
        FetchAnimeOngoingListUsecase(source = animeListSource)

    private val fetchAnimeAnnouncedListUsecase =
        FetchAnimeAnnouncedListUsecase(source = animeListSource)

    private val fetchAnimeListBySearchUsecase =
        FetchAnimeListBySearchUsecase(source = animeListSource)

    private val controller: AnimeListController by lazy {
        AnimeListController(
            storeFactory = DefaultStoreFactory(),
            lifecycle = essentyLifecycle(),
            usecases = getUsecases()
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

    private fun getUsecases() = Usecases(
        fetchAnimeOngoingListUsecase = fetchAnimeOngoingListUsecase,
        fetchAnimeAnnouncedListUsecase = fetchAnimeAnnouncedListUsecase,
        fetchAnimeListBySearchUsecase = fetchAnimeListBySearchUsecase
    )
}