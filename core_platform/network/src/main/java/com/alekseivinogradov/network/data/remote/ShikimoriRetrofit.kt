package com.alekseivinogradov.network.data.remote

private const val SHIKIMORI_BASE_URL = "https://shikimori.one"

val shikimoriRetrofit = retrofitBuilder
    .baseUrl(SHIKIMORI_BASE_URL)
    .build()