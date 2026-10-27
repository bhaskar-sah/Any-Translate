package com.bhaskar.anytranslate.data.api

import com.bhaskar.anytranslate.data.model.Language
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class OnlineTranslationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Translates text between any two supported languages via free public API.
     */
    suspend fun translate(text: String, from: Language, to: Language): Result<String> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(text.trim(), "UTF-8")
            val langPair = "${from.code}|${to.code}"
            val url = "https://api.mymemory.translated.net/get?q=$encoded&langpair=$langPair"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AnyTranslateApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error: ${response.code}"))
            }

            val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            val parsed = gson.fromJson(bodyString, MyMemoryResponse::class.java)

            val translation = parsed.responseData?.translatedText
            if (!translation.isNullOrBlank() && !translation.contains("MYMEMORY WARNING")) {
                Result.success(translation)
            } else {
                Result.failure(Exception("Could not obtain translation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class MyMemoryResponse(
        @SerializedName("responseData") val responseData: ResponseData?,
        @SerializedName("responseStatus") val responseStatus: Int?
    )

    private data class ResponseData(
        @SerializedName("translatedText") val translatedText: String?
    )
}
