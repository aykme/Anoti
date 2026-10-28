package com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase

/** Opens a web page in the system's browser, outside the app. */
interface OpenWebPageUsecase {
    /**
     * Hands [url] to the system. Main thread only.
     *
     * @return whether an app took the page; `false` when none can open it.
     */
    suspend fun execute(url: String): Boolean
}
