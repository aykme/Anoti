package com.alekseivinogradov.anoti.animefavorites.kmp.api.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.mvikotlin.core.store.StoreFactory

/** What the anime-favorites screen's component takes from its parent. */
interface DiAnimeFavoritesDependencies {

    /** Builds the store the screen owns. */
    val storeFactory: StoreFactory

    /** Coroutine contexts the screen's executor runs on. */
    val coroutineContextProvider: CoroutineContextProvider

    /** Formats the air dates the screen shows. */
    val dateFormatter: DateFormatter

    /** The app-wide saved-anime store; the source of the favorites list. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** Refreshes the whole saved library once the screen asks for it. */
    val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase
}
