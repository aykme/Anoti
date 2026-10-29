package com.alekseivinogradov.anoti.webpageopener.ios.impl.domain.usecase

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import kotlin.coroutines.resume

private const val TAG = "OpenWebPageUsecase"

/**
 * Opens a web page through [openUrl], the system's opener. Main thread only.
 *
 * @param openUrl hands an address to the system and calls back once with whether an app took it.
 */
class OpenWebPageUsecaseImpl(
    private val openUrl: (url: NSURL, onResult: (opened: Boolean) -> Unit) -> Unit
) : OpenWebPageUsecase {

    override suspend fun execute(url: String): Boolean {
        val address = NSURL.URLWithString(url)
        if (address == null) {
            println("$ANOTI_TAG $TAG: the web page address does not parse")
            return false
        }
        return suspendCancellableCoroutine { continuation ->
            openUrl(address) { opened: Boolean ->
                println("$ANOTI_TAG $TAG: the system opened the web page: $opened")
                // A screen closed before the answer canceled the wait; the answer goes nowhere.
                if (continuation.isActive) continuation.resume(opened)
            }
        }
    }
}
