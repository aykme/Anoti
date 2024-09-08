package com.alekseivinogradov.anime_list.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_list.api.data.local.repository.AnimeListDatabaseRepository
import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.anime_list.impl.data.local.repository.AnimeListDatabaseRepositoryImpl
import com.alekseivinogradov.anime_list.impl.data.remote.source.AnimeListSourceImpl
import com.alekseivinogradov.anime_list.impl.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeAnnouncedListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anime_list.impl.presentation.AnimeListController
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiService
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_network_base.impl.remote.ShikimoriApiServiceImpl
import com.alekseivinogradov.database.api.data.AnimeDatabaseRepository
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory

class AnimeListFragment : Fragment() {

    private lateinit var binding: FragmentAnimeListBinding

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val animeListSource: AnimeListSource = AnimeListSourceImpl(
        service = shikimoriService,
        safeApi = SafeApiImpl
    )

    private val fetchAnimeOngoingListUsecase =
        FetchAnimeOngoingListUsecase(source = animeListSource)

    private val fetchAnimeAnnouncedListUsecase =
        FetchAnimeAnnouncedListUsecase(source = animeListSource)

    private val fetchAnimeListBySearchUsecase =
        FetchAnimeListBySearchUsecase(source = animeListSource)

    private val fetchAnimeByIdUsecase =
        FetchAnimeByIdUsecase(source = animeListSource)

    val animeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(view!!.context.applicationContext)
    }

    val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    val animeListDatabaseRepository: AnimeListDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeListDatabaseRepositoryImpl(animeDatabaseRepository)
            }

    val insertAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                InsertAnimeDatabaseItemUsecase(animeListDatabaseRepository)
            }

    val deleteAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                DeleteAnimeDatabaseItemUsecase(animeListDatabaseRepository)
            }

    val fetchAllAnimeDatabaseItemsFlowUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                FetchAllAnimeDatabaseItemsFlowUsecase(animeListDatabaseRepository)
            }

    private val controller: AnimeListController by lazy {
        AnimeListController(
            storeFactory = DefaultStoreFactory(),
            lifecycle = essentyLifecycle(),
            ongoingUsecases = getOngoingUsecases(),
            announcedUsecases = getAnnouncedUsecases(),
            searchUsecases = getSearchUsecases()
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

    private fun getOngoingUsecases() = OngoingUsecases(
        fetchAnimeOngoingListUsecase = fetchAnimeOngoingListUsecase,
        fetchAnimeByIdUsecase = fetchAnimeByIdUsecase,
        insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
        deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase,
        fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase
    )

    private fun getAnnouncedUsecases() = AnnouncedUsecases(
        fetchAnimeAnnouncedListUsecase = fetchAnimeAnnouncedListUsecase,
        insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
        deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase,
        fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase
    )

    private fun getSearchUsecases() = SearchUsecases(
        fetchAnimeListBySearchUsecase = fetchAnimeListBySearchUsecase,
        fetchAnimeByIdUsecase = fetchAnimeByIdUsecase,
        insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
        deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase,
        fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase
    )
}