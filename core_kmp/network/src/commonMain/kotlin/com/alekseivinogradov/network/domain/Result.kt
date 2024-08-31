package com.alekseivinogradov.network.domain

sealed interface Result {
    data class Success<T>(val body: T) : Result
    data class Error(val e: Throwable) : Result
}