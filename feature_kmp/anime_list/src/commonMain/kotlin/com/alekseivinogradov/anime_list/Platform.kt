package com.alekseivinogradov.anime_list

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform