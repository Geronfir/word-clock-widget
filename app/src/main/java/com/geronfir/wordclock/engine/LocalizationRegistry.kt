package com.geronfir.wordclock.engine

/**
 * The set of languages the app ships.
 *
 * Adding a language means adding one entry here plus its [WordVocabulary] and
 * [TimeLocalization]; nothing in the engine changes.
 *
 * A **word grid** is a language-specific physical layout and is optional: only a
 * language whose phrase reads left-to-right, top-to-bottom can be laid out as a
 * fixed grid. English qualifies. Indonesian does not — its phrase order differs
 * by case ("JAM TIGA LEBIH SEPULUH" for past but "JAM SETENGAH EMPAT" for the
 * half hour), which no single static matrix can spell out. Indonesian therefore
 * ships no grid and renders as flowing text; the widget falls back automatically.
 */
object LocalizationRegistry {

    private val vocabularies: Map<String, WordVocabulary> = mapOf(
        "en" to EnglishVocabulary,
        "id" to IndonesianVocabulary,
    )

    private val localizations: Map<String, TimeLocalization> = mapOf(
        "en" to EnglishTimeLocalization,
        "id" to IndonesianTimeLocalization,
    )

    /** Optional per-language word grids. Languages absent here use flowing text. */
    private val grids: Map<String, WordGrid> = mapOf(
        "en" to EnglishWordGrid.grid,
    )

    /** Languages offered in the configuration screen, in a stable order. */
    val selectableLanguageTags: List<String> = listOf("en", "id")

    val supportedLanguageTags: Set<String> get() = vocabularies.keys

    fun vocabulary(languageTag: String): WordVocabulary =
        vocabularies[languageTag] ?: vocabularies.getValue(DEFAULT_LANGUAGE)

    fun localization(languageTag: String): TimeLocalization =
        localizations[languageTag] ?: localizations.getValue(DEFAULT_LANGUAGE)

    /** The language's word grid, or `null` when it has none (use flowing text). */
    fun gridOrNull(languageTag: String): WordGrid? = grids[languageTag]

    /** True when the language has both a vocabulary and a formatter. */
    fun isFullySupported(languageTag: String): Boolean =
        vocabularies.containsKey(languageTag) && localizations.containsKey(languageTag)

    const val DEFAULT_LANGUAGE = "en"
}
