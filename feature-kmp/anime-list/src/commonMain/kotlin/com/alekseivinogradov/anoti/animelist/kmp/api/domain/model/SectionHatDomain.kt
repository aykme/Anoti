package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

import kotlinx.serialization.Serializable

/** Which section is currently selected on the anime list screen. */
@Serializable
enum class SectionHatDomain {
    ONGOINGS,
    ANNOUNCED,
    SEARCH
}
