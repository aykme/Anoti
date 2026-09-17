package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider

/**
 * Holds the callbacks for showing error system messages.
 *
 * @param makeConnectionErrorSystemMessage shows that a network/connection error occurred.
 * @param makeUnknownErrorSystemMessage shows that an unexpected error occurred.
 */
class SystemMessageProvider(
    val makeConnectionErrorSystemMessage: () -> Unit,
    val makeUnknownErrorSystemMessage: () -> Unit
)
