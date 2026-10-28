package com.alekseivinogradov.anoti.webpageopener.kmp.impl.domain.usecase.fake

import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase

/**
 * Opens nothing; records each address and answers [result].
 *
 * @param result what every request answers: whether an app took the page.
 */
class OpenWebPageUsecaseFake(private val result: Boolean = true) : OpenWebPageUsecase {

    /** Every address asked for, in order. */
    val openedUrls: List<String>
        get() = requests

    private val requests = mutableListOf<String>()

    override suspend fun execute(url: String): Boolean {
        requests += url
        return result
    }
}
