package com.geronfir.wordclock.engine

/**
 * The set of languages the app ships.
 *
 * Adding a language means adding one entry here plus its [WordVocabulary],
 * [WordGrid] and [TimeLocalization]; nothing in the widget or the engine changes.
 * That is the extensibility the spec asks for.
 */
object LocalizationRegistry {

    private val vocabularies: Map<String, WordVocabulary> = mapOf(
        "en" to EnglishVocabulary,
        "id" to IndonesianVocabulary,
    )

    private val grids: Map<String, WordGrid> = mapOf(
        "en" to EnglishWordGrid.grid,
        "id" to IndonesianWordGrid.grid,
    )

    private val localizations: Map<String, TimeLocalization> = mapOf(
        "en" to EnglishTimeLocalization,
        "id" to IndonesianTimeLocalization,
    )

    /** Languages offered in the configuration screen, in a stable order. */
    val selectableLanguageTags: List<String> = listOf("en", "id")

    val supportedLanguageTags: Set<String> get() = vocabularies.keys

    fun vocabulary(languageTag: String): WordVocabulary =
        vocabularies[languageTag] ?: vocabularies.getValue(DEFAULT_LANGUAGE)

    fun grid(languageTag: String): WordGrid =
        grids[languageTag] ?: grids.getValue(DEFAULT_LANGUAGE)

    fun localization(languageTag: String): TimeLocalization =
        localizations[languageTag] ?: localizations.getValue(DEFAULT_LANGUAGE)

    /** True when the language has a vocabulary, a grid and a formatter. */
    fun isFullySupported(languageTag: String): Boolean =
        vocabularies.containsKey(languageTag) &&
            grids.containsKey(languageTag) &&
            localizations.containsKey(languageTag)

    const val DEFAULT_LANGUAGE = "en"
}
