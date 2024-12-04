package com.alekseivinogradov.anime_database

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform