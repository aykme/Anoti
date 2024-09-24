package com.alekseivinogradov.anime_base

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform