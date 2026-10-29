package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.usecase.wrapper

import com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store.InfoDialogExecutorImpl
import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase

/**
 * The usecases [InfoDialogExecutorImpl] calls.
 *
 * @param openWebPageUsecase opens each link's page in the system browser.
 */
data class InfoDialogUsecases(val openWebPageUsecase: OpenWebPageUsecase)
