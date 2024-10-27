package com.alekseivinogradov.anime_base.api.domain

class ToastProvider(
    val getMakeConnectionErrorToastCallback: () -> Unit,
    val getMakeUnknownErrorToastCallback: () -> Unit
)
