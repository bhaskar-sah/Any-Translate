package com.bhaskar.anytranslate.data.ml

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
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

    private val germanTranslator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.GERMAN)
            .build()
        Translation.getClient(options)
    }

    private val hindiTranslator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()
        Translation.getClient(options)
    }

    /**
     * Pre-downloads or verifies models in the background.
     */
    suspend fun prepareModels() {
        _downloadState.value = ModelDownloadState.Downloading
        val conditions = DownloadConditions.Builder().build()

        try {
            downloadModel(germanTranslator, conditions)
            downloadModel(hindiTranslator, conditions)
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
     * Translates English text to German using ML Kit on-device neural model.
     */
    suspend fun translateToGerman(text: String): Result<String> = suspendCancellableCoroutine { continuation ->
        germanTranslator.translate(text)
            .addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(Result.success(result))
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Translates English text to Hindi using ML Kit on-device neural model.
     */
    suspend fun translateToHindi(text: String): Result<String> = suspendCancellableCoroutine { continuation ->
        hindiTranslator.translate(text)
            .addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(Result.success(result))
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Closes native translation clients when destroyed.
     */
    fun close() {
        germanTranslator.close()
        hindiTranslator.close()
    }
}
