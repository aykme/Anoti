package com.alekseivinogradov.anoti.main.impl.presentation.savedstate

import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.Serializable

/**
 * What a screen host keeps in its saved-state file: a root's state, with what it takes to trust
 * that state again.
 *
 * @param formatVersion the version of this envelope's own layout.
 * @param appVersion the app version that saved it. An update can change what the screens save.
 * @param windowSessionId the platform's session of the window the state was saved from.
 * @param state the root's saved state.
 */
@Serializable
internal class SavedStateEnvelope(
    val formatVersion: Int,
    val appVersion: String,
    val windowSessionId: String,
    val state: SerializableContainer
) {
    companion object {
        /** The [formatVersion] this code writes and accepts. */
        const val FORMAT_VERSION = 1
    }
}
