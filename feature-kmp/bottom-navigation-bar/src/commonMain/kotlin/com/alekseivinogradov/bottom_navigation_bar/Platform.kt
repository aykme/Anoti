package com.alekseivinogradov.bottom_navigation_bar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform