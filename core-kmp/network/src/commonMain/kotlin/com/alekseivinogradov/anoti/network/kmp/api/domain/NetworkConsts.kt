package com.alekseivinogradov.anoti.network.kmp.api.domain

/** Base URL of the Shikimori API the app talks to. */
const val SHIKIMORI_BASE_URL = "https://shikimori.io"

// Shikimori asks every client to name its registered OAuth application here, and may ban the IP
// of one that doesn't.
const val SHIKIMORI_USER_AGENT = "Anoti"

// A single attempt fails fast instead of hanging on the engine's own default timeout, so
// SafeApi's retry budget stays bounded and predictable.
const val PER_ATTEMPT_TIMEOUT_MILLIS = 4000L
