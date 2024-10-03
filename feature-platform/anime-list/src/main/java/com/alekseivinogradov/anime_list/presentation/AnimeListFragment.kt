package com.alekseivinogradov.anime_list.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.animeListPlatform.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.impl.data.source.AnimeListSourceImpl
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anime_list.impl.presentation.AnimeListController
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.impl.domain.usecase.DatabaseUsecases
import com.alekseivinogradov.database.impl.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.database.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.database.impl.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.date.formatter.DateFormatter
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AnimeListFragment : Fragment() {

    private val tag = "AnimeList"

    private lateinit var binding: FragmentAnimeListBinding

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val animeListSource: AnimeListSource = AnimeListSourceImpl(
        service = shikimoriService,
        safeApi = SafeApiImpl
    )

    private val fetchOngoingAnimeListUsecase =
        FetchOngoingAnimeListUsecase(source = animeListSource)

    private val fetchAnnouncedAnimeListUsecase =
        FetchAnnouncedAnimeListUsecase(source = animeListSource)

    private val fetchAnimeListBySearchUsecase =
        FetchAnimeListBySearchUsecase(source = animeListSource)

    private val fetchAnimeByIdUsecase =
        FetchAnimeByIdUsecase(source = animeListSource)

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

    private val controller: AnimeListController by lazy {
        AnimeListController(
            storeFactory = DefaultStoreFactory(),
            lifecycle = essentyLifecycle(),
            databaseUsecases = getDatabaseUsecases(),
            ongoingUsecases = getOngoingUsecases(),
            announcedUsecases = getAnnouncedUsecases(),
            searchUsecases = getSearchUsecases()
        )
    }

    private val viewScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main +
                CoroutineExceptionHandler { _, e ->
                    Log.e(tag, "$e")
                }
    )

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
                viewBinding = binding,
                dateFormatter = DateFormatter.getInstance(view.context),
                viewScope = viewScope
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroy() {
        viewScope.cancel()
        super.onDestroy()
    }

    private fun getDatabaseUsecases() = DatabaseUsecases(
        fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase,
        insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
        deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase
    )

    private fun getOngoingUsecases() = OngoingUsecases(
        fetchOngoingAnimeListUsecase = fetchOngoingAnimeListUsecase,
        fetchAnimeByIdUsecase = fetchAnimeByIdUsecase
    )

    private fun getAnnouncedUsecases() = AnnouncedUsecases(
        fetchAnnouncedAnimeListUsecase = fetchAnnouncedAnimeListUsecase
    )

    private fun getSearchUsecases() = SearchUsecases(
        fetchAnimeListBySearchUsecase = fetchAnimeListBySearchUsecase,
        fetchAnimeByIdUsecase = fetchAnimeByIdUsecase
    )
}
