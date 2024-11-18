package com.alekseivinogradov.anime_list.impl.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.api.domain.provider.ToastProvider
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.data.source.AnimeListSourceImpl
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anime_list_platform.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.toast.AnotiToast
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.alekseivinogradov.database.api.domain.usecase.ChangeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.database.api.domain.usecase.DeleteDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsFlowUsecase
import com.alekseivinogradov.database.api.domain.usecase.InsertDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.ResetAllDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.database.api.domain.usecase.UpdateDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.wrapper.DatabaseUsecases
import com.alekseivinogradov.database.impl.domain.store.DatabaseExecutorImpl
import com.alekseivinogradov.database.impl.domain.store.DatabaseStoreFactory
import com.alekseivinogradov.database.impl.domain.usecase.ChangeDatabaseItemNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.DeleteDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.FetchAllDatabaseItemsFlowUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.InsertDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.ResetAllDatabaseItemsNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.UpdateDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory

class AnimeListFragment : Fragment() {

    private var binding: FragmentAnimeListBinding? = null

    private val storeFactory: StoreFactory = DefaultStoreFactory()

    private val coroutineContextProvider: CoroutineContextProvider by lazy {
        CoroutineContextProviderPlatform(requireContext().applicationContext)
    }

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val safeApi: SafeApi = SafeApiImpl

    private val animeDatabase: AnimeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(requireContext().applicationContext)
    }

    private val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    private val fetchAllDatabaseItemsFlowUsecase: FetchAllDatabaseItemsFlowUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                FetchAllDatabaseItemsFlowUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val insertDatabaseItemUsecase: InsertDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                InsertDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val deleteDatabaseItemUsecase: DeleteDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                DeleteDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val resetAllDatabaseItemsNewEpisodeStatusUsecase
            : ResetAllDatabaseItemsNewEpisodeStatusUsecase by lazy(LazyThreadSafetyMode.NONE) {
        ResetAllDatabaseItemsNewEpisodeStatusUsecaseImpl(repository = animeDatabaseRepository)
    }

    private val changeDatabaseItemNewEpisodeStatusUsecase
            : ChangeDatabaseItemNewEpisodeStatusUsecase by lazy(LazyThreadSafetyMode.NONE) {
        ChangeDatabaseItemNewEpisodeStatusUsecaseImpl(repository = animeDatabaseRepository)
    }

    private val updateDatabaseItemUsecase: UpdateDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                UpdateDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val databaseUsecases by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseUsecases(
            fetchAllDatabaseItemsFlowUsecase = fetchAllDatabaseItemsFlowUsecase,
            insertDatabaseItemUsecase = insertDatabaseItemUsecase,
            deleteDatabaseItemUsecase = deleteDatabaseItemUsecase,
            resetAllDatabaseItemsNewEpisodeStatusUsecase =
            resetAllDatabaseItemsNewEpisodeStatusUsecase,
            changeDatabaseItemNewEpisodeStatusUsecase = changeDatabaseItemNewEpisodeStatusUsecase,
            updateDatabaseItemUsecase = updateDatabaseItemUsecase
        )
    }

    private val animeListSource: AnimeListSource = AnimeListSourceImpl(
        service = shikimoriService,
        safeApi = safeApi
    )

    private val fetchOngoingAnimeListUsecase =
        FetchOngoingAnimeListUsecase(source = animeListSource)

    private val fetchAnnouncedAnimeListUsecase =
        FetchAnnouncedAnimeListUsecase(source = animeListSource)

    private val fetchAnimeListBySearchUsecase =
        FetchAnimeListBySearchUsecase(source = animeListSource)

    private val fetchAnimeDetailsByIdUsecase =
        FetchAnimeDetailsByIdUsecase(source = animeListSource)

    private val ongoingUsecases: OngoingUsecases = createOngoingUsecases()

    private val announcedUsecases: AnnouncedUsecases = createAnnouncedUsecases()

    private val searchUsecases: SearchUsecases = createSearchUsecases()

    private val toastProvider: ToastProvider = createToastProvider()

    private val animeListExecutorFactory: () -> AnimeListExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createAnimeListExecutorFactory() }

    private val mainStore: AnimeListMainStore by lazy(LazyThreadSafetyMode.NONE) {
        AnimeListMainStoreFactory(
            storeFactory = storeFactory,
            executorFactory = animeListExecutorFactory
        ).create()
    }

    private val databaseExecutorFactory: () -> DatabaseExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createDatabaseExecutorFactory() }

    private val databaseStore: DatabaseStore by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseStoreFactory(
            storeFactory = storeFactory,
            executorFactory = databaseExecutorFactory
        ).create()
    }

    private val ongoingSectionExecutorFactory: () -> OngoingSectionExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createOngoingSectionExecutorFactory() }

    private val ongoingSectionStore: OngoingSectionStore by lazy(LazyThreadSafetyMode.NONE) {
        OngoingSectionStoreFactory(
            storeFactory = storeFactory,
            executorFactory = ongoingSectionExecutorFactory
        ).create()
    }

    private val announcedSectionExecutorFactory: () -> AnnouncedSectionExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createAnnouncedSectionExecutorFactory() }

    private val announcedSectionStore: AnnouncedSectionStore by lazy(LazyThreadSafetyMode.NONE) {
        AnnouncedSectionStoreFactory(
            storeFactory = storeFactory,
            executorFactory = announcedSectionExecutorFactory
        ).create()
    }

    private val searchSectionExecutorFactory: () -> SearchSectionExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createSearchSectionExecutorFactory() }

    private val searchSectionStore: SearchSectionStore by lazy(LazyThreadSafetyMode.NONE) {
        SearchSectionStoreFactory(
            storeFactory = storeFactory,
            executorFactory = searchSectionExecutorFactory
        ).create()
    }

    private val controller: AnimeListController by lazy {
        AnimeListController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            databaseStore = databaseStore,
            ongoingSectionStore = ongoingSectionStore,
            announcedSectionStore = announcedSectionStore,
            searchSectionStore = searchSectionStore
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
                viewBinding = binding!!,
                dateFormatter = DateFormatter.getInstance(view.context),
                viewScope = lifecycleScope,
                coroutineContextProvider = coroutineContextProvider
            ),
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    private fun createOngoingUsecases() = OngoingUsecases(
        fetchOngoingAnimeListUsecase = fetchOngoingAnimeListUsecase,
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
    )

    private fun createAnnouncedUsecases() = AnnouncedUsecases(
        fetchAnnouncedAnimeListUsecase = fetchAnnouncedAnimeListUsecase
    )

    private fun createSearchUsecases() = SearchUsecases(
        fetchAnimeListBySearchUsecase = fetchAnimeListBySearchUsecase,
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
    )

    private fun createToastProvider() = ToastProvider(
        getMakeConnectionErrorToastCallback = {
            AnotiToast.makeConnectionErrorToast(requireContext().applicationContext)
        },
        getMakeUnknownErrorToastCallback = {
            AnotiToast.makeUnknownErrorToast(requireContext().applicationContext)
        }
    )

    private fun createAnimeListExecutorFactory(): () -> AnimeListExecutorImpl = {
        AnimeListExecutorImpl(
            coroutineContextProvider = coroutineContextProvider
        )
    }

    private fun createDatabaseExecutorFactory(): () -> DatabaseExecutorImpl = {
        DatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = databaseUsecases
        )
    }

    private fun createOngoingSectionExecutorFactory(): () -> OngoingSectionExecutorImpl = {
        OngoingSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = ongoingUsecases,
            toastProvider = toastProvider
        )
    }


    private fun createAnnouncedSectionExecutorFactory(): () -> AnnouncedSectionExecutorImpl = {
        AnnouncedSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = announcedUsecases,
            toastProvider = toastProvider
        )
    }


    private fun createSearchSectionExecutorFactory(): () -> SearchSectionExecutorImpl = {
        SearchSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = searchUsecases,
            toastProvider = toastProvider
        )
    }
}
