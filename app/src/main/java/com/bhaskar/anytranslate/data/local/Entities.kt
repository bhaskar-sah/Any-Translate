package com.bhaskar.anytranslate.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dictionary",
    indices = [
        Index(value = ["english_word"]),
        Index(value = ["german_meaning"]),
        Index(value = ["hindi_meaning"]),
        Index(value = ["nepali_meaning"])
    ]
)
data class DictionaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "english_word")
    val englishWord: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String? = null,

    @ColumnInfo(name = "german_meaning")
    val germanMeaning: String,

    @ColumnInfo(name = "german_phonetic")
    val germanPhonetic: String? = null,

    @ColumnInfo(name = "hindi_meaning")
    val hindiMeaning: String,

    @ColumnInfo(name = "hindi_translit")
    val hindiTranslit: String? = null,

    @ColumnInfo(name = "nepali_meaning")
    val nepaliMeaning: String,

    @ColumnInfo(name = "nepali_translit")
    val nepaliTranslit: String? = null,

    @ColumnInfo(name = "example_en")
    val exampleEn: String? = null,

    @ColumnInfo(name = "example_de")
    val exampleDe: String? = null,

    @ColumnInfo(name = "example_hi")
    val exampleHi: String? = null,

    @ColumnInfo(name = "example_ne")
    val exampleNe: String? = null
)

@Entity(
    tableName = "translation_history",
    indices = [Index(value = ["query_text"])]
)
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "query_text")
    val queryText: String,

    @ColumnInfo(name = "source_lang")
    val sourceLang: String = "en",

    @ColumnInfo(name = "english_translation")
    val englishTranslation: String = "",

    @ColumnInfo(name = "german_translation")
    val germanTranslation: String = "",

    @ColumnInfo(name = "nepali_translation")
    val nepaliTranslation: String = "",

    @ColumnInfo(name = "hindi_translation")
    val hindiTranslation: String = "",

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
