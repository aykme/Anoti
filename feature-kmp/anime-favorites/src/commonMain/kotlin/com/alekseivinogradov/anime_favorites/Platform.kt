package com.alekseivinogradov.anime_favorites

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform