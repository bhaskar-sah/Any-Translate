package com.bhaskar.anytranslate

import com.bhaskar.anytranslate.data.model.CombinedTranslation
import com.bhaskar.anytranslate.data.model.Language
import com.bhaskar.anytranslate.data.model.SingleTranslation
import com.bhaskar.anytranslate.data.model.TranslationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryDataTest {

    @Test
    fun testLanguageEnumCodes() {
        assertEquals("en", Language.ENGLISH.code)
        assertEquals("de", Language.GERMAN.code)
        assertEquals("ne", Language.NEPALI.code)
        assertEquals("hi", Language.HINDI.code)
    }

    @Test
    fun testFromCodeLookup() {
        assertEquals(Language.ENGLISH, Language.fromCode("en"))
        assertEquals(Language.GERMAN, Language.fromCode("de"))
        assertEquals(Language.NEPALI, Language.fromCode("ne"))
        assertEquals(Language.HINDI, Language.fromCode("hi"))
    }

    @Test
    fun testCombinedTranslationFromGermanInput() {
        val germanInput = "Guten Morgen"
        val translations = listOf(
            SingleTranslation(
                language = Language.ENGLISH,
                translatedText = "Good morning",
                partOfSpeech = "phrase"
            ),
            SingleTranslation(
                language = Language.NEPALI,
                translatedText = "शुभ प्रभात",
                phonetic = "Subha Prabhat"
            ),
            SingleTranslation(
                language = Language.HINDI,
                translatedText = "शुभ प्रभात",
                phonetic = "Shubh Prabhat"
            )
        )

        val combined = CombinedTranslation(
            query = germanInput,
            sourceLanguage = Language.GERMAN,
            isAutoDetected = true,
            translations = translations
        )

        assertEquals(Language.GERMAN, combined.sourceLanguage)
        assertTrue(combined.isAutoDetected)
        assertEquals(3, combined.translations.size)
        assertEquals("Good morning", combined.getTranslation(Language.ENGLISH)?.translatedText)
        assertEquals("शुभ प्रभात", combined.getTranslation(Language.NEPALI)?.translatedText)
        assertEquals("शुभ प्रभात", combined.getTranslation(Language.HINDI)?.translatedText)
    }

    @Test
    fun testNepaliDevanagariSupport() {
        val nepaliTranslation = SingleTranslation(
            language = Language.NEPALI,
            translatedText = "नमस्ते",
            phonetic = "Namaste",
            partOfSpeech = "interjection",
            source = TranslationSource.OFFLINE_DICT
        )

        assertEquals("नमस्ते", nepaliTranslation.translatedText)
        assertEquals("Namaste", nepaliTranslation.phonetic)
    }
}
