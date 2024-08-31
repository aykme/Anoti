package com.alekseivinogradov.network

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform