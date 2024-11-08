package com.alekseivinogradov.anime_background_update

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform