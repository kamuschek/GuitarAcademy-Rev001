package com.example.api

import com.example.BuildConfig
import com.example.data.PracticeSession
import com.example.data.UserProgress
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Models for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- API Client implementation ---

object GeminiCoachService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Sends the practice profile to Gemini to obtain a personalized coaching session.
     */
    suspend fun getCoachingFeedback(
        prompt: String,
        progress: UserProgress?,
        history: List<PracticeSession>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return "Guitar Coach API Key is set to dummy placeholder! Set your real GEMINI_API_KEY in the Secrets panel in AI Studio to activate live feedback. \n\nHere is some local coach advice:\nKeep your index finger close to the metal fret wire for clean chords. Try to play with your tips rather than the fleshy pads to prevent adjacent string damping!"
        }

        // Build a detailed progress analysis log
        val statsContext = StringBuilder()
        statsContext.append("STUDENT HISTORICAL DATALOGS:\n")
        statsContext.append("- Current practice streak: ${progress?.streak ?: 0} days\n")
        statsContext.append("- Total practice duration: ${progress?.totalPracticeMinutes ?: 0} minutes\n")
        statsContext.append("- Strength areas: ${progress?.strengthsList ?: "None"}\n")
        statsContext.append("- Needed growth areas: ${progress?.weaknessesList ?: "None"}\n\n")

        statsContext.append("RECENT OFF-LINE SESSIONS:\n")
        if (history.isEmpty()) {
            statsContext.append("(No logs entries recorded yet)\n")
        } else {
            history.take(4).forEach { item ->
                statsContext.append("- Date: ${item.date}, Duration: ${item.durationMinutes}m, Focus: ${item.focusArea}, Attempted: ${item.chordsAttempted}, Acc: ${item.accuracyScore}%\n")
            }
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = "Student query: $prompt\n\n$statsContext")))
            ),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(
                parts = listOf(
                    Part(
                        text = "You are 'Fretwise AI', an expert virtual guitar coach. Review the student's data and query carefully. State actionable advice with an encouraging, highly professional, and encouraging voice. Pinpoint and comment on their listed strengths or areas of growth. Format your responses elegantly in simple scannable bullet points where possible."
                    )
                )
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "My strings are out of sync... Please draft another query or check internet permissions!"
        } catch (e: Exception) {
            "An error occurred while calling the Gemini Coach: ${e.message}\n\nLocal Tip: Keep your posture straight, relax your wrist, and angle your thumb vertically behind the neck to avoid cramp."
        }
    }
}
