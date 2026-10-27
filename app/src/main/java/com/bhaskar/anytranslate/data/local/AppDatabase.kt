package com.bhaskar.anytranslate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(
    entities = [DictionaryEntity::class, TranslationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "any_translate.db"
                )
                    .addCallback(DatabasePrepopulationCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulationCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                prepopulateDictionary(context)
            }
        }

        private suspend fun prepopulateDictionary(context: Context) {
            try {
                val inputStream = context.assets.open("dictionary.json")
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<DictionaryRawItem>>() {}.type
                val rawItems: List<DictionaryRawItem> = Gson().fromJson(reader, type)

                val entities = rawItems.map { raw ->
                    DictionaryEntity(
                        englishWord = raw.english,
                        partOfSpeech = raw.part_of_speech,
                        germanMeaning = raw.german,
                        germanPhonetic = raw.german_phonetic,
                        hindiMeaning = raw.hindi,
                        hindiTranslit = raw.hindi_translit,
                        nepaliMeaning = raw.nepali,
                        nepaliTranslit = raw.nepali_translit,
                        exampleEn = raw.example_en,
                        exampleDe = raw.example_de,
                        exampleHi = raw.example_hi,
                        exampleNe = raw.example_ne
                    )
                }

                getInstance(context).dictionaryDao().insertAll(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private data class DictionaryRawItem(
        val english: String,
        val part_of_speech: String? = null,
        val german: String,
        val german_phonetic: String? = null,
        val hindi: String,
        val hindi_translit: String? = null,
        val nepali: String,
        val nepali_translit: String? = null,
        val example_en: String? = null,
        val example_de: String? = null,
        val example_hi: String? = null,
        val example_ne: String? = null
    )
}
