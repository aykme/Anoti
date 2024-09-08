package com.alekseivinogradov.network.impl.data

import com.alekseivinogradov.network.api.domain.SHIKIMORI_BASE_URL

val shikimoriRetrofit = retrofitBuilder
    .baseUrl(SHIKIMORI_BASE_URL)
    .build()