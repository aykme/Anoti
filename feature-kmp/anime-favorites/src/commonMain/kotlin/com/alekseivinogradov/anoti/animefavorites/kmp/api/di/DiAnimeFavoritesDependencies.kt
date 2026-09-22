package com.alekseivinogradov.anoti.animefavorites.kmp.api.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

/** What the anime-favorites screen's component takes from its parent. */
interface DiAnimeFavoritesDependencies {

    /** Builds the store the screen owns. */
    val storeFactory: StoreFactory

    /** Coroutine contexts the screen's executor runs on. */
    val coroutineContextProvider: CoroutineContextProvider

    /** Where the screen reports connection and unknown errors. */
    val systemMessageProvider: SystemMessageProvider

    /** Formats the air dates the screen shows. */
    val dateFormatter: DateFormatter

    /** The app-wide saved-anime store; the source of the favorites list. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The anime API the screen fetches per-item details from. */
    val shikimoriApiService: ShikimoriApiService

    /** Wraps every call to [shikimoriApiService] with retries and error classification. */
    val safeApi: SafeApi

    /** Refreshes the whole saved library once the screen asks for it. */
    val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase
}
