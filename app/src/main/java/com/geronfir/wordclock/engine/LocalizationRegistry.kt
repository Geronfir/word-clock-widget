package com.geronfir.wordclock.engine

/**
 * The set of languages the app ships.
 *
 * Adding a language means adding one entry here plus its [WordVocabulary] and
 * [WordGrid]; nothing in the widget or the engine changes. That is the
 * extensibility the spec asks for.
 */
object LocalizationRegistry {

    private val vocabularies: Map<String, WordVocabulary> = mapOf(
        "en" to EnglishVocabulary,
    )

    private val grids: Map<String, WordGrid> = mapOf(
        "en" to EnglishWordGrid.grid,
    )

    val supportedLanguageTags: Set<String> get() = vocabularies.keys

    fun vocabulary(languageTag: String): WordVocabulary =
        vocabularies[languageTag] ?: vocabularies.getValue(DEFAULT_LANGUAGE)

    fun grid(languageTag: String): WordGrid =
        grids[languageTag] ?: grids.getValue(DEFAULT_LANGUAGE)

    /** True when the language has both a vocabulary and a grid. */
    fun isFullySupported(languageTag: String): Boolean =
        vocabularies.containsKey(languageTag) && grids.containsKey(languageTag)

    const val DEFAULT_LANGUAGE = "en"
}
