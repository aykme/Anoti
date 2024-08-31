package com.alekseivinogradov.anime_network_base

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform