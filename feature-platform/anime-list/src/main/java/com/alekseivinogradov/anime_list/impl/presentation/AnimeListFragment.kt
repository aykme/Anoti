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
import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_database.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anime_database.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anime_database.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anime_database.impl.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.DeleteAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.InsertAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.UpdateAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anime_database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.anime_database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
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
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlin.time.Duration.Companion.milliseconds

class AnimeListFragment : Fragment() {

    private var binding: FragmentAnimeListBinding? = null

    private val storeFactory: StoreFactory = DefaultStoreFactory()

    private val coroutineContextProvider: CoroutineContextProvider by lazy {
        CoroutineContextProviderPlatform(requireContext().applicationContext)
    }

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val safeApi: SafeApi = SafeApiImpl(
        maxAttempt = 3,
        attemptDelay = 2500.milliseconds
    )

    private val animeDatabase: AnimeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(requireContext().applicationContext)
    }

    private val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    private val fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                FetchAllAnimeDatabaseItemsFlowUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                InsertAnimeDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                DeleteAnimeDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
            : ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl(
                    repository = animeDatabaseRepository
                )
            }

    private val changeAnimeDatabaseItemNewEpisodeStatusUsecase
            : ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl(
                    repository = animeDatabaseRepository
                )
            }

    private val updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                UpdateAnimeDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val animeDatabaseUsecases by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabaseUsecases(
            fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase,
            insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
            deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase,
            resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase =
            resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase,
            changeAnimeDatabaseItemNewEpisodeStatusUsecase =
            changeAnimeDatabaseItemNewEpisodeStatusUsecase,
            updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase
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

    private val animeDatabaseExecutorFactory: () -> AnimeDatabaseExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createAnimeDatabaseExecutorFactory() }

    private val animeDatabaseStore: AnimeDatabaseStore by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabaseStoreFactory(
            storeFactory = storeFactory,
            executorFactory = animeDatabaseExecutorFactory
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
            animeDatabaseStore = animeDatabaseStore,
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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

    private fun createAnimeDatabaseExecutorFactory(): () -> AnimeDatabaseExecutorImpl = {
        AnimeDatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = animeDatabaseUsecases
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
