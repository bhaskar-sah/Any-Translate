package com.bhaskar.anytranslate.data.ml

import android.content.Context
import com.bhaskar.anytranslate.data.model.Language
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class ModelDownloadState {
    object Idle : ModelDownloadState()
    object Downloading : ModelDownloadState()
    object Ready : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}

class MLKitTranslationManager(private val context: Context) {

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
    val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    private val languageIdentifier: LanguageIdentifier by lazy {
        LanguageIdentification.getClient()
    }

    private val translatorCache = ConcurrentHashMap<String, Translator>()

    /**
     * Detects language of input text (returns Language or null if undetermined).
     */
    suspend fun identifyLanguage(text: String): Language? = suspendCancellableCoroutine { continuation ->
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val detected = when (languageCode) {
                    "de" -> Language.GERMAN
                    "hi" -> Language.HINDI
                    "ne" -> Language.NEPALI
                    "en" -> Language.ENGLISH
                    else -> detectLanguageFromScriptOrPhonetics(text)
                }
                if (continuation.isActive) continuation.resume(detected)
            }
            .addOnFailureListener {
                val fallback = detectLanguageFromScriptOrPhonetics(text)
                if (continuation.isActive) continuation.resume(fallback)
            }
    }

    private fun detectLanguageFromScriptOrPhonetics(text: String): Language? {
        val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
        if (hasDevanagari) {
            // Distinguish Nepali specific markers if present, otherwise default to Hindi
            val nepaliMarkers = listOf("छ", "छन्", "हुन्छ", "हुनुहुन्छ", "तपाईं", "राम्रो", "होइन", "के")
            val isNepali = nepaliMarkers.any { text.contains(it) }
            return if (isNepali) Language.NEPALI else Language.HINDI
        }

        // German specific characters
        val germanChars = listOf('ä', 'ö', 'ü', 'ß', 'Ä', 'Ö', 'Ü')
        if (text.any { it in germanChars }) {
            return Language.GERMAN
        }

        return null
    }

    private fun getTranslator(source: Language, target: Language): Translator? {
        val sourceCode = mapToMLKitLanguage(source) ?: return null
        val targetCode = mapToMLKitLanguage(target) ?: return null

        val key = "${sourceCode}_$targetCode"
        return translatorCache.computeIfAbsent(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()
            Translation.getClient(options)
        }
    }

    private fun mapToMLKitLanguage(lang: Language): String? {
        return when (lang) {
            Language.ENGLISH -> TranslateLanguage.ENGLISH
            Language.GERMAN -> TranslateLanguage.GERMAN
            Language.HINDI -> TranslateLanguage.HINDI
            Language.NEPALI -> null // ML Kit doesn't have local offline Nepali pack
        }
    }

    /**
     * Prepares common language models in the background.
     */
    suspend fun prepareModels() {
        _downloadState.value = ModelDownloadState.Downloading
        val conditions = DownloadConditions.Builder().build()

        try {
            getTranslator(Language.ENGLISH, Language.GERMAN)?.let { downloadModel(it, conditions) }
            getTranslator(Language.ENGLISH, Language.HINDI)?.let { downloadModel(it, conditions) }
            _downloadState.value = ModelDownloadState.Ready
        } catch (e: Exception) {
            _downloadState.value = ModelDownloadState.Error(e.message ?: "Failed downloading models")
        }
    }

    private suspend fun downloadModel(translator: Translator, conditions: DownloadConditions) =
        suspendCancellableCoroutine<Unit> { continuation ->
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }

    /**
     * Translates text between any two supported ML Kit languages.
     */
    suspend fun translate(text: String, from: Language, to: Language): Result<String> {
        val translator = getTranslator(from, to)
            ?: return Result.failure(UnsupportedOperationException("Language pair not supported by ML Kit"))

        return suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(Result.success(result))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }
    }

    fun close() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
        languageIdentifier.close()
    }
}
