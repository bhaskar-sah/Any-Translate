package com.bhaskar.anytranslate.data.repository

import com.bhaskar.anytranslate.data.api.OnlineTranslationService
import com.bhaskar.anytranslate.data.local.DictionaryDao
import com.bhaskar.anytranslate.data.local.DictionaryEntity
import com.bhaskar.anytranslate.data.local.HistoryDao
import com.bhaskar.anytranslate.data.local.TranslationHistoryEntity
import com.bhaskar.anytranslate.data.ml.MLKitTranslationManager
import com.bhaskar.anytranslate.data.model.CombinedTranslation
import com.bhaskar.anytranslate.data.model.Language
import com.bhaskar.anytranslate.data.model.SingleTranslation
import com.bhaskar.anytranslate.data.model.TranslationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TranslationRepository(
    private val dictionaryDao: DictionaryDao,
    private val historyDao: HistoryDao,
    private val mlKitManager: MLKitTranslationManager,
    private val onlineService: OnlineTranslationService
) {

    val historyFlow: Flow<List<TranslationHistoryEntity>> = historyDao.getAllHistory()
    val favoritesFlow: Flow<List<TranslationHistoryEntity>> = historyDao.getFavorites()

    /**
     * Translates input query from either an explicit language or auto-detected language
     * into all other supported languages.
     */
    suspend fun translate(query: String, explicitSourceLang: Language? = null): CombinedTranslation = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        val isBookmarked = historyDao.isFavorite(trimmedQuery) ?: false

        // 1. Check local offline dictionary across all columns first
        val dictEntry = dictionaryDao.findAnyMatch(trimmedQuery)
        if (dictEntry != null) {
            val resolvedSource = explicitSourceLang ?: determineSourceFromDict(trimmedQuery, dictEntry)
            val targets = Language.values().filter { it != resolvedSource }

            val translations = targets.map { target ->
                buildSingleFromDict(target, dictEntry, isBookmarked)
            }

            val combined = CombinedTranslation(
                query = trimmedQuery,
                sourceLanguage = resolvedSource,
                isAutoDetected = (explicitSourceLang == null),
                translations = translations
            )

            saveToHistory(combined, isBookmarked)
            return@withContext combined
        }

        // 2. Novel text / full sentences: Detect language if not explicitly provided
        val resolvedSource = explicitSourceLang ?: (mlKitManager.identifyLanguage(trimmedQuery) ?: Language.ENGLISH)
        val targets = Language.values().filter { it != resolvedSource }

        val deferredTranslations = targets.map { target ->
            async {
                // Try ML Kit on-device first
                val mlResult = mlKitManager.translate(trimmedQuery, resolvedSource, target)
                if (mlResult.isSuccess) {
                    SingleTranslation(
                        language = target,
                        translatedText = mlResult.getOrNull().orEmpty(),
                        source = TranslationSource.ML_KIT,
                        isBookmarked = isBookmarked
                    )
                } else {
                    // Fall back to cloud translation
                    val onlineResult = onlineService.translate(trimmedQuery, resolvedSource, target)
                    if (onlineResult.isSuccess) {
                        SingleTranslation(
                            language = target,
                            translatedText = onlineResult.getOrNull().orEmpty(),
                            source = TranslationSource.ONLINE_CLOUD,
                            isBookmarked = isBookmarked
                        )
                    } else {
                        SingleTranslation(
                            language = target,
                            translatedText = trimmedQuery,
                            phonetic = "Translation unavailable offline",
                            source = TranslationSource.OFFLINE_DICT,
                            isBookmarked = isBookmarked
                        )
                    }
                }
            }
        }

        val translations = deferredTranslations.awaitAll()

        val combined = CombinedTranslation(
            query = trimmedQuery,
            sourceLanguage = resolvedSource,
            isAutoDetected = (explicitSourceLang == null),
            translations = translations
        )

        saveToHistory(combined, isBookmarked)
        return@withContext combined
    }

    private fun determineSourceFromDict(query: String, entry: DictionaryEntity): Language {
        val q = query.lowercase()
        return when {
            entry.englishWord.lowercase() == q -> Language.ENGLISH
            entry.germanMeaning.lowercase() == q -> Language.GERMAN
            entry.nepaliMeaning.lowercase() == q || entry.nepaliTranslit?.lowercase() == q -> Language.NEPALI
            entry.hindiMeaning.lowercase() == q || entry.hindiTranslit?.lowercase() == q -> Language.HINDI
            else -> Language.ENGLISH
        }
    }

    private fun buildSingleFromDict(target: Language, entry: DictionaryEntity, isBookmarked: Boolean): SingleTranslation {
        return when (target) {
            Language.ENGLISH -> SingleTranslation(
                language = Language.ENGLISH,
                translatedText = entry.englishWord,
                partOfSpeech = entry.partOfSpeech,
                exampleTarget = entry.exampleEn,
                exampleEnglish = entry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )
            Language.GERMAN -> SingleTranslation(
                language = Language.GERMAN,
                translatedText = entry.germanMeaning,
                phonetic = entry.germanPhonetic,
                partOfSpeech = entry.partOfSpeech,
                exampleTarget = entry.exampleDe,
                exampleEnglish = entry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )
            Language.NEPALI -> SingleTranslation(
                language = Language.NEPALI,
                translatedText = entry.nepaliMeaning,
                phonetic = entry.nepaliTranslit,
                partOfSpeech = entry.partOfSpeech,
                exampleTarget = entry.exampleNe,
                exampleEnglish = entry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )
            Language.HINDI -> SingleTranslation(
                language = Language.HINDI,
                translatedText = entry.hindiMeaning,
                phonetic = entry.hindiTranslit,
                partOfSpeech = entry.partOfSpeech,
                exampleTarget = entry.exampleHi,
                exampleEnglish = entry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )
        }
    }

    private suspend fun saveToHistory(result: CombinedTranslation, isBookmarked: Boolean) {
        val english = result.getTranslation(Language.ENGLISH)?.translatedText
            ?: if (result.sourceLanguage == Language.ENGLISH) result.query else ""
        val german = result.getTranslation(Language.GERMAN)?.translatedText
            ?: if (result.sourceLanguage == Language.GERMAN) result.query else ""
        val nepali = result.getTranslation(Language.NEPALI)?.translatedText
            ?: if (result.sourceLanguage == Language.NEPALI) result.query else ""
        val hindi = result.getTranslation(Language.HINDI)?.translatedText
            ?: if (result.sourceLanguage == Language.HINDI) result.query else ""

        historyDao.insertHistory(
            TranslationHistoryEntity(
                queryText = result.query,
                sourceLang = result.sourceLanguage.code,
                englishTranslation = english,
                germanTranslation = german,
                nepaliTranslation = nepali,
                hindiTranslation = hindi,
                isFavorite = isBookmarked
            )
        )
    }

    suspend fun toggleFavorite(query: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        historyDao.updateFavoriteByQuery(query, isFavorite)
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAll()
    }
}
