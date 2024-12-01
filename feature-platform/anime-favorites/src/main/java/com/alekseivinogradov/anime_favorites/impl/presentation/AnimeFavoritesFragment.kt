package com.alekseivinogradov.anime_favorites.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anime_background_update.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.api.domain.provider.ToastProvider
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.impl.data.source.AnimeFavoritesSourceImpl
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anime_favorites.impl.presentation.di.AnimeFavoritesComponent
import com.alekseivinogradov.anime_favorites.impl.presentation.di.DaggerAnimeFavoritesComponent
import com.alekseivinogradov.anime_favorites_platform.databinding.FragmentAnimeFavoritesBinding
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.toast.AnotiToast
import com.alekseivinogradov.di.api.presentation.main.MainActivityExternal
import com.alekseivinogradov.di.api.presentation.scope.FeatureScope
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.core.store.StoreFactory
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@FeatureScope
class AnimeFavoritesFragment : Fragment() {

    private lateinit var animeFavoritesComponent: AnimeFavoritesComponent

    private var binding: FragmentAnimeFavoritesBinding? = null

    @Inject
    internal lateinit var storeFactory: StoreFactory

    @Inject
    internal lateinit var coroutineContextProvider: CoroutineContextProvider

    @Inject
    internal lateinit var shikimoriService: ShikimoriApiService

    @Inject
    internal lateinit var safeApi: SafeApi

    @Inject
    internal lateinit var updateAllAnimeInBackgroundOnceUsecase:
            UpdateAllAnimeInBackgroundOnceUsecase

    private val animeFavoritesSource: AnimeFavoritesSource = AnimeFavoritesSourceImpl(
        service = ShikimoriApiServiceImpl(
            servicePlatform = ShikimoriApiServicePlatform.instance
        ),
        safeApi = SafeApiImpl(
            maxAttempt = 3,
            attemptDelay = 2500.milliseconds
        )
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

    @Inject
    lateinit var animeDatabaseStore: AnimeDatabaseStore

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        )
    }

    override fun onAttach(context: Context) {
        animeFavoritesComponent = DaggerAnimeFavoritesComponent.factory().create(
            appComponent = (this.activity as MainActivityExternal).mainComponent
        ).also { it.inject(fragment = this) }
        super.onAttach(context)
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
        updateAllAnimeInBackgroundOnceUsecase = updateAllAnimeInBackgroundOnceUsecase
    )

    private fun createToastProvider() = ToastProvider(
        getMakeConnectionErrorToastCallback = {
            AnotiToast.makeConnectionErrorToast(requireContext().applicationContext)
        },
        getMakeUnknownErrorToastCallback = {
            AnotiToast.makeUnknownErrorToast(requireContext().applicationContext)
        }
    )

    private fun createAnimeFavoritesExecutorFactory(): () -> AnimeFavoritesExecutorImpl = {
        AnimeFavoritesExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = favoritesUsecases,
            toastProvider = toastProvider
        )
    }
}
