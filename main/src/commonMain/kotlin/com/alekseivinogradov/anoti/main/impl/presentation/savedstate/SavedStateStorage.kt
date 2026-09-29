package com.alekseivinogradov.anoti.main.impl.presentation.savedstate

import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.json.Json

private const val TAG = "SavedStateStorage"

/**
 * Keeps a root's state across the end of the process, for a platform that saves none itself.
 * The state is trusted again only from the same app version and the same window session. The
 * platform keeps that session across a system kill but not across the user closing the app.
 *
 * @param file where the state is kept.
 * @param appVersion the running app's version.
 * @param sceneSessionId reads the platform's session of the window the app shows in, or `null`
 * when there is none.
 */
internal class SavedStateStorage(
    private val file: SavedStateFile,
    private val appVersion: String,
    private val sceneSessionId: () -> String?
) {

    /**
     * Writes the [state] of the root numbered [rootNumber]. Without a window session nothing is
     * written, and an older file stays. A save that fails leaves no file, so an older state
     * cannot reopen an older screen.
     */
    fun save(rootNumber: Int, state: () -> SerializableContainer) {
        val sessionId = sceneSessionId()
        if (sessionId == null) {
            println("$TAG: root $rootNumber not saved, there is no window session")
            return
        }
        runCatching {
            // Encoding runs the screens' serializers, since the saved state is kept lazily.
            Json.encodeToString(
                SavedStateEnvelope.serializer(),
                SavedStateEnvelope(
                    formatVersion = SavedStateEnvelope.FORMAT_VERSION,
                    appVersion = appVersion,
                    sceneSessionId = sessionId,
                    state = state()
                )
            )
        }.onSuccess { text: String ->
            if (file.write(text)) {
                println("$TAG: saved root $rootNumber, session $sessionId")
            }
        }.onFailure { throwable: Throwable ->
            file.delete()
            println("$TAG: root $rootNumber was not saved: $throwable")
        }
    }

    /**
     * Takes the saved state out of the file, or gives `null` when there is none to trust. The
     * file is gone afterward either way. A state that crashes the app on restore then crashes it
     * once.
     *
     * @param isDiscarded the state is not wanted, but the file is taken all the same.
     */
    fun take(isDiscarded: Boolean): SerializableContainer? {
        val text = file.take() ?: return null
        val sessionId = sceneSessionId()
        val envelope = runCatching {
            Json.decodeFromString(SavedStateEnvelope.serializer(), text)
        }.getOrNull()
        val isAccepted = !isDiscarded &&
            envelope?.formatVersion == SavedStateEnvelope.FORMAT_VERSION &&
            envelope.appVersion == appVersion &&
            sessionId != null &&
            envelope.sceneSessionId == sessionId
        println(
            "$TAG: saved state restored: $isAccepted, discarded: $isDiscarded, " +
                "readable: ${envelope != null}, saved session ${envelope?.sceneSessionId}, " +
                "current session $sessionId"
        )
        return envelope?.state?.takeIf { isAccepted }
    }
}
