package com.alekseivinogradov.anime_base.api.domain.provider

class ToastProvider(
    val getMakeConnectionErrorToastCallback: () -> Unit,
    val getMakeUnknownErrorToastCallback: () -> Unit
)
