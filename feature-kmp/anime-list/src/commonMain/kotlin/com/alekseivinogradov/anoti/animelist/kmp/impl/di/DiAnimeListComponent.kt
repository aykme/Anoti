package com.alekseivinogradov.anoti.animelist.kmp.impl.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.AnimeListSourceImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Provides

/**
 * The anime-list screen's [FeatureScope] component. Provides the [AnimeListSource], its usecases
 * and the main/announced/ongoing/search section stores, on top of the bindings it inherits from
 * [parent].
 */
// One function per provided dependency is the DI @Provides convention here, not god-interface growth.
@Suppress("TooManyFunctions")
@Component
@FeatureScope
abstract class DiAnimeListComponent(
    @Component val parent: DiAnimeListDependencies
) {
    /** The app-wide [CoroutineContextProvider], inherited from the parent. */
    abstract val coroutineContextProvider: CoroutineContextProvider

    /** The [DateFormatter], inherited from the parent. */
    abstract val dateFormatter: DateFormatter

    /** The app-wide [AnimeDatabaseStore], inherited from the parent. */
    abstract val animeDatabaseStore: AnimeDatabaseStore

    /** The screen's [AnimeListMainStore]. */
    abstract val mainStore: AnimeListMainStore

    /** The screen's [OngoingSectionStore]. */
    abstract val ongoingSectionStore: OngoingSectionStore

    /** The screen's [AnnouncedSectionStore]. */
    abstract val announcedSectionStore: AnnouncedSectionStore

    /** The screen's [SearchSectionStore]. */
    abstract val searchSectionStore: SearchSectionStore

    @Provides
    fun provideAnimeListSource(service: ShikimoriApiService, safeApi: SafeApi): AnimeListSource =
        AnimeListSourceImpl(service = service, safeApi = safeApi)

    @Provides
    fun provideFetchAnimeDetailsByIdUsecase(source: AnimeListSource): FetchAnimeDetailsByIdUsecase =
        FetchAnimeDetailsByIdUsecase(source)

    @Provides
    fun provideAnimeListExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider
    ): AnimeListExecutorFactory = { AnimeListExecutorImpl(coroutineContextProvider = coroutineContextProvider) }

    @Provides
    fun provideAnimeListMainStore(
        storeFactory: StoreFactory,
        executorFactory: AnimeListExecutorFactory
    ): AnimeListMainStore = AnimeListMainStoreFactory(storeFactory, executorFactory).create()

    @Provides
    fun provideFetchAnnouncedAnimeListUsecase(source: AnimeListSource): FetchAnnouncedAnimeListUsecase =
        FetchAnnouncedAnimeListUsecase(source)

    @Provides
    fun provideAnnouncedUsecases(
        fetchAnnouncedAnimeListUsecase: FetchAnnouncedAnimeListUsecase
    ): AnnouncedUsecases = AnnouncedUsecases(fetchAnnouncedAnimeListUsecase = fetchAnnouncedAnimeListUsecase)

    @Provides
    fun provideAnnouncedSectionExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        usecases: AnnouncedUsecases,
        toastProvider: ToastProvider
    ): AnnouncedSectionExecutorFactory = {
        AnnouncedSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

    @Provides
    fun provideAnnouncedSectionStore(
        storeFactory: StoreFactory,
        executorFactory: AnnouncedSectionExecutorFactory
    ): AnnouncedSectionStore = AnnouncedSectionStoreFactory(storeFactory, executorFactory).create()

    @Provides
    fun provideFetchOngoingAnimeListUsecase(source: AnimeListSource): FetchOngoingAnimeListUsecase =
        FetchOngoingAnimeListUsecase(source)

    @Provides
    fun provideOngoingUsecases(
        fetchOngoingAnimeListUsecase: FetchOngoingAnimeListUsecase,
        fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
    ): OngoingUsecases = OngoingUsecases(
        fetchOngoingAnimeListUsecase = fetchOngoingAnimeListUsecase,
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
    )

    @Provides
    fun provideOngoingSectionExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        usecases: OngoingUsecases,
        toastProvider: ToastProvider
    ): OngoingSectionExecutorFactory = {
        OngoingSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

    @Provides
    fun provideOngoingSectionStore(
        storeFactory: StoreFactory,
        executorFactory: OngoingSectionExecutorFactory
    ): OngoingSectionStore = OngoingSectionStoreFactory(storeFactory, executorFactory).create()

    @Provides
    fun provideFetchAnimeListBySearchUsecase(source: AnimeListSource): FetchAnimeListBySearchUsecase =
        FetchAnimeListBySearchUsecase(source)

    @Provides
    fun provideSearchUsecases(
        fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
        fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
    ): SearchUsecases = SearchUsecases(
        fetchAnimeListBySearchUsecase = fetchAnimeListBySearchUsecase,
        fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
    )

    @Provides
    fun provideSearchSectionExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        usecases: SearchUsecases,
        toastProvider: ToastProvider
    ): SearchSectionExecutorFactory = {
        SearchSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

    @Provides
    fun provideSearchSectionStore(
        storeFactory: StoreFactory,
        executorFactory: SearchSectionExecutorFactory
    ): SearchSectionStore = SearchSectionStoreFactory(storeFactory, executorFactory).create()
}

@KmpComponentCreate
expect fun createDiAnimeListComponent(parent: DiAnimeListDependencies): DiAnimeListComponent
