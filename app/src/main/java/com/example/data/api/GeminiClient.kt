package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Generate content given a prompt
     */
    suspend fun generate(prompt: String, jsonMode: Boolean = false): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "ERROR: Gemini API Key is unconfigured. Please configure it in the Secrets panel."
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = if (jsonMode) GeminiGenerationConfig(responseMimeType = "application/json") else null
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response text"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "Unknown API Error"}"
        }
    }

    /**
     * Generate suggested replies based on recent conversation transcripts.
     */
    suspend fun getSuggestedReplies(transcriptText: String): List<String> {
        val prompt = """
            You are a real-time call assistant co-pilot. Based on the conversation transcript below with Speaker A, suggest 3 elegant, short, practical replies that the user can select to speak back.
            Keep each reply under 15 words.
            You must return your response STRICTLY as a JSON array of strings. Do not include any markdown format or triple backticks.
            Example: ["I have received the tracking information. Thank you.", "I am checking on that right now.", "Can you please repeat the reference number?"]
            
            Transcript so far:
            $transcriptText
        """.trimIndent()

        val response = generate(prompt, jsonMode = true)
        return try {
            // Parse JSON array of strings
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(listType)
            adapter.fromJson(response) ?: emptyList()
        } catch (e: Exception) {
            // Fallback in case of parse failure
            listOf(
                "Got it, looking into this now.",
                "Let me double check that for you.",
                "Yes, sounds good. Thank you!"
            )
        }
    }

    /**
     * Generate summary for call sessions.
     */
    suspend fun getCallSummary(transcriptText: String): String {
        val prompt = """
            Summarize the following call transcript into a professional, concise summary. Highlight key decisions and action items in clean markdown bullet points. Keep it clear and compact.
            
            Transcript:
            $transcriptText
        """.trimIndent()

        return generate(prompt, jsonMode = false)
    }
}
