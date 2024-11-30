package com.alekseivinogradov.anime_favorites.impl.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
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
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.domain.usecase.UpdateAllAnimeInBackgroundUsecase
import com.alekseivinogradov.anime_favorites.impl.data.source.AnimeFavoritesSourceImpl
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.UpdateAllAnimeInBackgroundUsecaseImpl
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
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

class AnimeFavoritesFragment : Fragment() {

    private var binding: FragmentAnimeFavoritesBinding? = null

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

    private val animeUpdateOnceWork: OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<AnimeUpdateWorker>().build()

    private val updateAllAnimeInBackgroundUsecase: UpdateAllAnimeInBackgroundUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                UpdateAllAnimeInBackgroundUsecaseImpl(
                    workManager = WorkManager.getInstance(requireContext().applicationContext),
                    updateWork = animeUpdateOnceWork,
                    uniqueWorkName = AnimeUpdateWorker.animeUpdateOnceWorkName
                )
            }

    private val animeFavoritesSource: AnimeFavoritesSource = AnimeFavoritesSourceImpl(
        service = shikimoriService,
        safeApi = safeApi
    )

    private val fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(
        source = animeFavoritesSource
    )

    private val favoritesUsecases by lazy(LazyThreadSafetyMode.NONE) {
        createFavoritesUsecases()
    }

    private val toastProvider: ToastProvider = createToastProvider()

    private val animeFavoritesExecutorFactory: () -> AnimeFavoritesExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createAnimeFavoritesExecutorFactory() }

    private val mainStore: AnimeFavoritesMainStore by lazy(LazyThreadSafetyMode.NONE) {
        AnimeFavoritesMainStoreFactory(
            storeFactory = storeFactory,
            executorFactory = animeFavoritesExecutorFactory
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

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun createFavoritesUsecases() = FavoritesUsecases(
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase,
        updateAllAnimeInBackgroundUsecase = updateAllAnimeInBackgroundUsecase
    )

    private fun createToastProvider() = ToastProvider(
        getMakeConnectionErrorToastCallback = {
            AnotiToast.makeConnectionErrorToast(requireContext().applicationContext)
        },
        getMakeUnknownErrorToastCallback = {
            AnotiToast.makeUnknownErrorToast(requireContext().applicationContext)
        }
    )

    private fun createAnimeDatabaseExecutorFactory(): () -> AnimeDatabaseExecutorImpl = {
        AnimeDatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = animeDatabaseUsecases
        )
    }

    private fun createAnimeFavoritesExecutorFactory(): () -> AnimeFavoritesExecutorImpl = {
        AnimeFavoritesExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = favoritesUsecases,
            toastProvider = toastProvider
        )
    }
}
