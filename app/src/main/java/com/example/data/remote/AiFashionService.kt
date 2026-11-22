package com.example.data.remote

import android.util.Log
import com.example.model.Product
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

sealed interface AiInferenceState<out T> {
    data object Idle : AiInferenceState<Nothing>
    data object Loading : AiInferenceState<Nothing>
    data class Success<T>(val data: T) : AiInferenceState<T>
    data class Error(val message: String) : AiInferenceState<Nothing>
}

data class AiDressRecommendationResult(
    val recommendedProductIds: List<String>,
    val stylingAdvice: String,
    val occasionMatchNote: String
)

data class AiSmartSearchResult(
    val matchedProductIds: List<String>,
    val searchSummary: String
)

/**
 * Real cloud AI assistant & smart search powered by Firebase AI Logic (Gemini API).
 * Zero mocked responses: failures surface through AiInferenceState.Error for user retry.
 */
class AiFashionService {

    companion object {
        private const val TAG = "AiFashionService"
        private const val MODEL_NAME = "gemini-3.8-flash"
        private const val TIMEOUT_MS = 60_000L
    }

    private val recommendationSchema = Schema.obj(
        properties = mapOf(
            "recommendedProductIds" to Schema.array(
                items = Schema.string(),
                description = "Exact product IDs from the provided GYANVI DRESSES catalog that best match the buyer request"
            ),
            "stylingAdvice" to Schema.string(
                description = "Helpful Hindi-English styling tip explaining colors, fabric, jewelry or footwear pairings"
            ),
            "occasionMatchNote" to Schema.string(
                description = "Concise one-line badge summarizing the occasion & budget fit"
            )
        )
    )

    private val searchSchema = Schema.obj(
        properties = mapOf(
            "matchedProductIds" to Schema.array(
                items = Schema.string(),
                description = "Ordered list of matching product IDs from the provided catalog (best matches first)"
            ),
            "searchSummary" to Schema.string(
                description = "Short explanation in clear Hinglish/English of why these outfits match the query"
            )
        )
    )

    private val stylistModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = recommendationSchema
                temperature = 0.4f
            }
        )
    }

    private val searchModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = searchSchema
                temperature = 0.2f
            }
        )
    }

    private fun buildCompactCatalogPrompt(catalog: List<Product>): String {
        return catalog.take(60).joinToString(separator = "\n") { p ->
            "- id=\"${p.id}\", name=\"${p.name}\", category=\"${p.category}\", price=₹${p.price}, mrp=₹${p.originalPrice}, rating=${p.rating}, sizes=${p.sizes.joinToString("/")}, desc=\"${p.description.take(90)}\""
        }
    }

    suspend fun getDressRecommendations(
        userQuery: String,
        occasion: String,
        maxBudget: Int,
        catalog: List<Product>
    ): Result<AiDressRecommendationResult> {
        return try {
            val catalogSummary = buildCompactCatalogPrompt(catalog)
            val prompt = """
                You are the fashion stylist for GYANVI DRESSES, an Indian ethnic & western wear e-commerce marketplace.
                Buyer request: "$userQuery"
                Occasion: "$occasion"
                Maximum budget: ₹$maxBudget

                Available catalog products:
                $catalogSummary

                Select up to 6 most suitable products strictly using their exact `id` values from the catalog above.
                Prefer items at or below ₹$maxBudget when available.
                Write warm, practical stylingAdvice in Hinglish/English (2-3 sentences) and a crisp occasionMatchNote.
            """.trimIndent()

            val response = withTimeout(TIMEOUT_MS) {
                stylistModel.generateContent(prompt)
            }
            val rawText = response.text?.trim()
                ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))

            val json = JSONObject(rawText)
            val idsArray = json.optJSONArray("recommendedProductIds") ?: JSONArray()
            val validCatalogIds = catalog.map { it.id }.toSet()
            val matchedIds = buildList {
                for (i in 0 until idsArray.length()) {
                    val id = idsArray.optString(i).trim()
                    if (id.isNotEmpty() && id in validCatalogIds && id !in this) {
                        add(id)
                    }
                }
            }
            val stylingAdvice = json.optString("stylingAdvice", "").trim()
            val occasionNote = json.optString("occasionMatchNote", "").trim()

            Result.success(
                AiDressRecommendationResult(
                    recommendedProductIds = matchedIds,
                    stylingAdvice = stylingAdvice.ifBlank { "Selected outfits matched to your occasion and budget." },
                    occasionMatchNote = occasionNote.ifBlank { "$occasion • Under ₹$maxBudget" }
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI dress recommendation timed out", e)
            Result.failure(Exception("AI stylist request timed out. Please check your internet connection and retry."))
        } catch (e: Exception) {
            Log.e(TAG, "AI dress recommendation failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Unable to fetch AI recommendations right now."))
        }
    }

    suspend fun smartProductSearch(
        query: String,
        catalog: List<Product>
    ): Result<AiSmartSearchResult> {
        return try {
            val catalogSummary = buildCompactCatalogPrompt(catalog)
            val prompt = """
                You are the AI semantic search engine for GYANVI DRESSES.
                User natural-language search query (could be Hindi, Hinglish, or English, with budget/color/occasion constraints): "$query"

                Available catalog products:
                $catalogSummary

                Return the matching product `id`s ranked by relevance (up to 12) and a short searchSummary explaining what was matched.
                Only include exact `id` values present in the catalog above.
            """.trimIndent()

            val response = withTimeout(TIMEOUT_MS) {
                searchModel.generateContent(prompt)
            }
            val rawText = response.text?.trim()
                ?: return Result.failure(IllegalStateException("AI search returned an empty response."))

            val json = JSONObject(rawText)
            val idsArray = json.optJSONArray("matchedProductIds") ?: JSONArray()
            val validCatalogIds = catalog.map { it.id }.toSet()
            val matchedIds = buildList {
                for (i in 0 until idsArray.length()) {
                    val id = idsArray.optString(i).trim()
                    if (id.isNotEmpty() && id in validCatalogIds && id !in this) {
                        add(id)
                    }
                }
            }
            val summary = json.optString("searchSummary", "").trim()
            Result.success(
                AiSmartSearchResult(
                    matchedProductIds = matchedIds,
                    searchSummary = summary.ifBlank { "Showing AI-matched outfits for \"$query\"" }
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI smart search timed out", e)
            Result.failure(Exception("AI Search timed out. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "AI smart search failed", e)
            Result.failure(Exception(e.localizedMessage ?: "AI Search is currently unavailable."))
        }
    }
}
