package com.alekseivinogradov.anoti.main.api.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.controller.SystemMessageController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

/**
 * What the root UI graph and its screen children take from the app-wide component. The app-wide
 * component implements it, so neither side has to know the other's concrete type.
 */
interface DiRootDependencies {
    /** The MVIKotlin factory every store is created through. */
    val storeFactory: StoreFactory

    /** The coroutine contexts and dispatchers the app's work runs on. */
    val coroutineContextProvider: CoroutineContextProvider

    /** The callbacks that show an error message to the user. */
    val systemMessageProvider: SystemMessageProvider

    /** The stream of system messages the on-screen host shows. */
    val systemMessageController: SystemMessageController

    /** Formats date strings for display. */
    val dateFormatter: DateFormatter

    /** The store over the saved anime. Every read hands out a new one. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The Shikimori endpoints for the anime list and an anime's details. */
    val shikimoriApiService: ShikimoriApiService

    /** Runs a network call with retries and returns its outcome as a `CallResult`. */
    val safeApi: SafeApi

    /** Triggers a one-off background update of every saved anime. */
    val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase
}
