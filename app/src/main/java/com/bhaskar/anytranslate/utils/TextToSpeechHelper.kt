package com.bhaskar.anytranslate.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.bhaskar.anytranslate.data.model.Language
import java.util.Locale

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
        }
    }

    fun speak(text: String, language: Language) {
        if (!isInitialized || tts == null) {
            Toast.makeText(context, "Text-to-speech engine initializing...", Toast.LENGTH_SHORT).show()
            return
        }

        val locale = when (language) {
            Language.ENGLISH -> Locale.US
            Language.GERMAN -> Locale.GERMAN
            Language.HINDI -> Locale("hi", "IN")
            Language.NEPALI -> {
                val neLocale = Locale("ne", "NP")
                val result = tts?.setLanguage(neLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Locale("hi", "IN")
                } else {
                    neLocale
                }
            }
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "Voice language not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AnyTranslateTTS_${System.currentTimeMillis()}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
