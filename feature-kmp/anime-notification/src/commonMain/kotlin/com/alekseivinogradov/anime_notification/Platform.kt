package com.alekseivinogradov.anime_notification

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform