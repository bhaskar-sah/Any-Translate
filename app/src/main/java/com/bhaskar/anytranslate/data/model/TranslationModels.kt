package com.bhaskar.anytranslate.data.model

enum class Language(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇬🇧"),
    GERMAN("de", "German", "🇩🇪"),
    NEPALI("ne", "Nepali", "🇳🇵"),
    HINDI("hi", "Hindi", "🇮🇳");

    companion object {
        fun fromCode(code: String): Language? {
            return when (code.lowercase().trim()) {
                "en" -> ENGLISH
                "de" -> GERMAN
                "ne" -> NEPALI
                "hi" -> HINDI
                else -> null
            }
        }
    }
}

enum class TranslationSource(val label: String) {
    OFFLINE_DICT("Offline Dict"),
    ML_KIT("AI On-Device"),
    ONLINE_CLOUD("Cloud AI")
}

data class SingleTranslation(
    val language: Language,
    val translatedText: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val exampleTarget: String? = null,
    val exampleEnglish: String? = null,
    val source: TranslationSource = TranslationSource.OFFLINE_DICT,
    val isBookmarked: Boolean = false
)

data class CombinedTranslation(
    val query: String,
    val sourceLanguage: Language,
    val isAutoDetected: Boolean = false,
    val translations: List<SingleTranslation>,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getTranslation(language: Language): SingleTranslation? =
        translations.find { it.language == language }
}
