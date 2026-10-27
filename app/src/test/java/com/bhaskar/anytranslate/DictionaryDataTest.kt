package com.bhaskar.anytranslate

import com.bhaskar.anytranslate.data.model.Language
import com.bhaskar.anytranslate.data.model.SingleTranslation
import com.bhaskar.anytranslate.data.model.TranslationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DictionaryDataTest {

    @Test
    fun testLanguageEnumCodes() {
        assertEquals("de", Language.GERMAN.code)
        assertEquals("ne", Language.NEPALI.code)
        assertEquals("hi", Language.HINDI.code)
    }

    @Test
    fun testSingleTranslationCreation() {
        val translation = SingleTranslation(
            language = Language.GERMAN,
            translatedText = "Guten Morgen",
            phonetic = "GOO-ten MOR-gen",
            partOfSpeech = "phrase",
            exampleTarget = "Guten Morgen an alle Schüler.",
            exampleEnglish = "Good morning to all students.",
            source = TranslationSource.OFFLINE_DICT,
            isBookmarked = true
        )

        assertEquals("Guten Morgen", translation.translatedText)
        assertEquals(Language.GERMAN, translation.language)
        assertEquals(TranslationSource.OFFLINE_DICT, translation.source)
        assertEquals(true, translation.isBookmarked)
        assertNotNull(translation.exampleTarget)
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
