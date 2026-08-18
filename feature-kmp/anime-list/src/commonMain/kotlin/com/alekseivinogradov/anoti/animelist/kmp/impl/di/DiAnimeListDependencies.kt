package com.alekseivinogradov.anoti.animelist.kmp.impl.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

/** What the anime-list screen's component takes from its parent. */
interface DiAnimeListDependencies {
    val storeFactory: StoreFactory
    val coroutineContextProvider: CoroutineContextProvider
    val toastProvider: ToastProvider
    val dateFormatter: DateFormatter
    val animeDatabaseStore: AnimeDatabaseStore
    val shikimoriApiService: ShikimoriApiService
    val safeApi: SafeApi
}
