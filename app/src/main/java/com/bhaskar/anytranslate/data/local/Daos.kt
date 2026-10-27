package com.bhaskar.anytranslate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary WHERE LOWER(english_word) = LOWER(:word) LIMIT 1")
    suspend fun findByWord(word: String): DictionaryEntity?

    @Query("SELECT * FROM dictionary WHERE LOWER(english_word) LIKE LOWER(:prefix || '%') ORDER BY english_word ASC LIMIT 10")
    suspend fun searchPrefix(prefix: String): List<DictionaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DictionaryEntity>)

    @Query("SELECT COUNT(*) FROM dictionary")
    suspend fun getCount(): Int
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<TranslationHistoryEntity>>

    @Query("SELECT * FROM translation_history WHERE is_favorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<TranslationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: TranslationHistoryEntity): Long

    @Query("UPDATE translation_history SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE translation_history SET is_favorite = :isFavorite WHERE LOWER(query_text) = LOWER(:query)")
    suspend fun updateFavoriteByQuery(query: String, isFavorite: Boolean)

    @Query("SELECT is_favorite FROM translation_history WHERE LOWER(query_text) = LOWER(:query) LIMIT 1")
    suspend fun isFavorite(query: String): Boolean?

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()
}
