package com.alekseivinogradov.anoti.animelist.kmp.api.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

/** What the anime-list screen's component takes from its parent. */
interface DiAnimeListDependencies {

    /** Builds every store the screen owns. */
    val storeFactory: StoreFactory

    /** Coroutine contexts the screen's executors run on. */
    val coroutineContextProvider: CoroutineContextProvider

    /** Where the screen reports connection and unknown errors. */
    val systemMessageProvider: SystemMessageProvider

    /** Formats the air dates the screen shows. */
    val dateFormatter: DateFormatter

    /** The app-wide saved-anime store; drives the items' notification state. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The anime API the screen's lists are paged from. */
    val shikimoriApiService: ShikimoriApiService

    /** Wraps every call to [shikimoriApiService] with retries and error classification. */
    val safeApi: SafeApi
}
