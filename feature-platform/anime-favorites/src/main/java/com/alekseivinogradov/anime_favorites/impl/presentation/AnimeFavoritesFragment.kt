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

class AnimeFavoritesFragment : Fragment() {

    private var binding: FragmentAnimeFavoritesBinding? = null

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

    private val databaseExecutorFactory: () -> DatabaseExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createDatabaseExecutorFactory() }

    private val databaseStore: DatabaseStore by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseStoreFactory(
            storeFactory = storeFactory,
            executorFactory = databaseExecutorFactory
        ).create()
    }

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            databaseStore = databaseStore
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

    private fun createDatabaseExecutorFactory(): () -> DatabaseExecutorImpl = {
        DatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = databaseUsecases
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
