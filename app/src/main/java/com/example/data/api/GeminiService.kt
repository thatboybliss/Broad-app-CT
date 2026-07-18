package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generatePodcastScript(topic: String): PodcastGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiService", "API Key is missing or default placeholder!")
            return@withContext PodcastGenerationResult.Error("API Key is not configured. Please add GEMINI_API_KEY to your Secrets panel or .env file.")
        }

        val prompt = """
            You are "My Superstar AI Podcast" script generator.
            Generate an engaging, high-fidelity 4-line podcast mini-episode script about the topic: "$topic".
            Structure it as a lively dialogue between "Cosmic Gemini" (academic, vision-driven host) and "DJ Nebula" (sound-enthusiastic, trend-driven co-host).
            
            You MUST return ONLY a raw JSON block and absolutely nothing else. No markdown headers, no explanation, no trailing text.
            The JSON structure must match this EXACT format:
            {
              "episodeTitle": "Title of the Episode",
              "description": "Short 1-sentence catchy summary of this episode.",
              "dialogue": [
                {
                  "speaker": "Cosmic Gemini",
                  "text": "First host sentence here, opening the topic with style."
                },
                {
                  "speaker": "DJ Nebula",
                  "text": "Second speaker sentence responding with energy."
                },
                {
                  "speaker": "Cosmic Gemini",
                  "text": "Third sentence providing deeper insights."
                },
                {
                  "speaker": "DJ Nebula",
                  "text": "Concluding co-host sentence summing up with enthusiasm."
                }
              ]
            }
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Enforce response_mime_type: application/json if supported, but structured prompting suffices.
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = response.body?.string() ?: "Unknown error"
                    Log.e("GeminiService", "API call failed with code ${response.code}: $errorMsg")
                    return@withContext PodcastGenerationResult.Error("API request failed with code ${response.code}")
                }

                val responseBody = response.body?.string() ?: return@withContext PodcastGenerationResult.Error("Empty response body")
                Log.d("GeminiService", "Response received: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() == 0) {
                    return@withContext PodcastGenerationResult.Error("No content returned from AI.")
                }

                val text = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Clean markdown JSON format wrappers if any (Gemini sometimes wraps json in ```json ... ``` even with mimeType)
                val cleanJsonString = cleanJsonMarkdown(text)

                try {
                    val parsedResult = JSONObject(cleanJsonString)
                    val title = parsedResult.optString("episodeTitle", "Cosmic Horizons")
                    val description = parsedResult.optString("description", "An immersive AI conversation exploring the edge of tech.")
                    val dialogueArray = parsedResult.getJSONArray("dialogue")

                    val dialogueList = mutableListOf<DialoguePart>()
                    for (i in 0 until dialogueArray.length()) {
                        val partObj = dialogueArray.getJSONObject(i)
                        dialogueList.add(
                            DialoguePart(
                                speaker = partObj.getString("speaker"),
                                text = partObj.getString("text")
                            )
                        )
                    }

                    PodcastGenerationResult.Success(
                        title = title,
                        description = description,
                        dialogue = dialogueList,
                        rawJson = cleanJsonString
                    )
                } catch (e: Exception) {
                    Log.e("GeminiService", "JSON parsing failed for string: $cleanJsonString", e)
                    PodcastGenerationResult.Error("Failed to parse the generated script. Please try again.")
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "API request exception", e)
            PodcastGenerationResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    private fun cleanJsonMarkdown(rawText: String): String {
        var result = rawText.trim()
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json")
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```")
        }
        if (result.endsWith("```")) {
            result = result.removeSuffix("```")
        }
        return result.trim()
    }
}

sealed class PodcastGenerationResult {
    data class Success(
        val title: String,
        val description: String,
        val dialogue: List<DialoguePart>,
        val rawJson: String
    ) : PodcastGenerationResult()
    data class Error(val message: String) : PodcastGenerationResult()
}

data class DialoguePart(
    val speaker: String,
    val text: String
)
