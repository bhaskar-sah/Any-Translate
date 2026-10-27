package com.bhaskar.anytranslate.data.repository

import com.bhaskar.anytranslate.data.api.OnlineTranslationService
import com.bhaskar.anytranslate.data.local.DictionaryDao
import com.bhaskar.anytranslate.data.local.HistoryDao
import com.bhaskar.anytranslate.data.local.TranslationHistoryEntity
import com.bhaskar.anytranslate.data.ml.MLKitTranslationManager
import com.bhaskar.anytranslate.data.model.CombinedTranslation
import com.bhaskar.anytranslate.data.model.Language
import com.bhaskar.anytranslate.data.model.SingleTranslation
import com.bhaskar.anytranslate.data.model.TranslationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
     * Translates the given English text into German, Nepali, and Hindi simultaneously.
     */
    suspend fun translate(query: String): CombinedTranslation = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        val isBookmarked = historyDao.isFavorite(trimmedQuery) ?: false

        // 1. Check local pre-populated offline dictionary first
        val dictEntry = dictionaryDao.findByWord(trimmedQuery)
        if (dictEntry != null) {
            val german = SingleTranslation(
                language = Language.GERMAN,
                translatedText = dictEntry.germanMeaning,
                phonetic = dictEntry.germanPhonetic,
                partOfSpeech = dictEntry.partOfSpeech,
                exampleTarget = dictEntry.exampleDe,
                exampleEnglish = dictEntry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )

            val nepali = SingleTranslation(
                language = Language.NEPALI,
                translatedText = dictEntry.nepaliMeaning,
                phonetic = dictEntry.nepaliTranslit,
                partOfSpeech = dictEntry.partOfSpeech,
                exampleTarget = dictEntry.exampleNe,
                exampleEnglish = dictEntry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )

            val hindi = SingleTranslation(
                language = Language.HINDI,
                translatedText = dictEntry.hindiMeaning,
                phonetic = dictEntry.hindiTranslit,
                partOfSpeech = dictEntry.partOfSpeech,
                exampleTarget = dictEntry.exampleHi,
                exampleEnglish = dictEntry.exampleEn,
                source = TranslationSource.OFFLINE_DICT,
                isBookmarked = isBookmarked
            )

            val combined = CombinedTranslation(
                query = trimmedQuery,
                german = german,
                nepali = nepali,
                hindi = hindi
            )

            saveToHistory(combined, isBookmarked)
            return@withContext combined
        }

        // 2. If not found in offline dictionary, run parallel ML Kit + Online Translation
        val germanDeferred = async {
            val mlResult = mlKitManager.translateToGerman(trimmedQuery)
            if (mlResult.isSuccess) {
                SingleTranslation(
                    language = Language.GERMAN,
                    translatedText = mlResult.getOrNull().orEmpty(),
                    source = TranslationSource.ML_KIT,
                    isBookmarked = isBookmarked
                )
            } else {
                SingleTranslation(
                    language = Language.GERMAN,
                    translatedText = trimmedQuery,
                    phonetic = "Translation unavailable offline",
                    source = TranslationSource.OFFLINE_DICT,
                    isBookmarked = isBookmarked
                )
            }
        }

        val hindiDeferred = async {
            val mlResult = mlKitManager.translateToHindi(trimmedQuery)
            if (mlResult.isSuccess) {
                SingleTranslation(
                    language = Language.HINDI,
                    translatedText = mlResult.getOrNull().orEmpty(),
                    source = TranslationSource.ML_KIT,
                    isBookmarked = isBookmarked
                )
            } else {
                SingleTranslation(
                    language = Language.HINDI,
                    translatedText = trimmedQuery,
                    phonetic = "Translation unavailable offline",
                    source = TranslationSource.OFFLINE_DICT,
                    isBookmarked = isBookmarked
                )
            }
        }

        val nepaliDeferred = async {
            val onlineResult = onlineService.translateEnglishToNepali(trimmedQuery)
            if (onlineResult.isSuccess) {
                SingleTranslation(
                    language = Language.NEPALI,
                    translatedText = onlineResult.getOrNull().orEmpty(),
                    source = TranslationSource.ONLINE_CLOUD,
                    isBookmarked = isBookmarked
                )
            } else {
                SingleTranslation(
                    language = Language.NEPALI,
                    translatedText = trimmedQuery,
                    phonetic = "Offline database lookup only",
                    source = TranslationSource.OFFLINE_DICT,
                    isBookmarked = isBookmarked
                )
            }
        }

        val german = germanDeferred.await()
        val hindi = hindiDeferred.await()
        val nepali = nepaliDeferred.await()

        val combined = CombinedTranslation(
            query = trimmedQuery,
            german = german,
            nepali = nepali,
            hindi = hindi
        )

        saveToHistory(combined, isBookmarked)
        return@withContext combined
    }

    private suspend fun saveToHistory(result: CombinedTranslation, isBookmarked: Boolean) {
        historyDao.insertHistory(
            TranslationHistoryEntity(
                queryText = result.query,
                germanTranslation = result.german.translatedText,
                nepaliTranslation = result.nepali.translatedText,
                hindiTranslation = result.hindi.translatedText,
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
