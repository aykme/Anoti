package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model

/** Loading state of the favorites list. */
sealed interface ContentTypeDomain {
    /**
     * @param hasMinimumDuration whether this loading state was entered from opening the section
     * or a pull-to-refresh — while true, the list's own arrival can't resolve it early, so it
     * stays visible for at least that action's minimum-visible duration.
     */
    data class LOADING(val hasMinimumDuration: Boolean = false) : ContentTypeDomain
    data object LOADED : ContentTypeDomain
    data object EMPTY : ContentTypeDomain
}
