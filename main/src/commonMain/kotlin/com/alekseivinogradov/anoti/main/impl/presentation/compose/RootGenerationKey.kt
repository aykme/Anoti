package com.alekseivinogradov.anoti.main.impl.presentation.compose

/**
 * The key [RootSessionContent] replaces its content by. It compares the generation, so the
 * content is rebuilt whenever the generation changes.
 *
 * Its hash stays the same across generations on purpose. Compose names every `rememberSaveable`
 * value after a hash that takes in this key's. A hash that followed the generation would rename
 * the saved values after a rebuild, and the next process, starting at generation 0, would find
 * none of them.
 */
internal class RootGenerationKey(val generation: Int) {

    override fun equals(other: Any?): Boolean =
        other is RootGenerationKey && other.generation == generation

    override fun hashCode(): Int = 0
}
