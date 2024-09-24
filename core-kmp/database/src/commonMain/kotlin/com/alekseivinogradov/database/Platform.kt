package com.alekseivinogradov.database

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform