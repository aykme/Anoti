package com.alekseivinogradov.anime_base.api.domain

class ToastProvider(
    val makeConnectionErrorToast: () -> Unit,
    val makeUnknownErrorToast: () -> Unit
)
