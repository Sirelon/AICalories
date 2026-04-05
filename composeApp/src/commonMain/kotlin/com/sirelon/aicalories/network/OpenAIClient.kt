package com.sirelon.aicalories.network

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseInputItem
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.client.OpenAI
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.data.GeneratedAdMapper
import com.sirelon.aicalories.network.responses.GeneratedAd
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


private const val PROMPT =
    """Analyze the provided images of a single item and generate a marketplace listing.

Return ONLY valid JSON:
{
  "title": "...",
  "description": "...",
  "suggestedPrice": number,
  "minPrice": number,
  "maxPrice": number,
  "condition": "new|like_new|good|fair|poor"
}

Rules:
- Use only information visible in the images
- Do not invent brand, model, or specifications
- If multiple images are provided, combine details from all of them
- Title: short and clear, include brand/model only if visible
- Description: 2–4 sentences, include condition, key details, and visible defects
- Pricing: estimate realistic second-hand market value in USD
  - suggestedPrice should be the best estimate
  - minPrice and maxPrice should define a reasonable range
- Condition must be exactly one of: new, like_new, good, fair, poor

Output:
- Write all text fields in Ukrainian
- Use numbers only for prices (no currency symbols, no text)
- Do not include any text outside JSON"""

class OpenAIClient(
    private val openAI: OpenAI,
    private val json: Json,
) {

    private val mapper = GeneratedAdMapper()

    suspend fun analyzeThing(images: List<String>): Advertisement {
        val response = openAI.response(
            request = ResponseRequest(
                model = ModelId("gpt-4.1"),
                temperature = 0.2,
                maxOutputTokens = 300,
                input = ResponseInput(
                    items = listOf(promptInput())
                            + images.map(::createImageResponseItem)
                ),
            )
        )

        val jsonString = response.outputText ?: response
            .output
            .joinToString(separator = "\n") {
                it.content
                    .orEmpty()
                    .joinToString(separator = "\n") {
                        it.text.orEmpty()
                    }
            }

        val generatedAd = json.decodeFromString<GeneratedAd>(jsonString)
        return mapper.mapToDomain(generatedAd, images)
    }

    private fun promptInput(): ResponseInputItem = ResponseInputItem(
        role = "system",
        content = buildJsonArray {
            add(
                buildJsonObject {
                    put("type", "input_text")
                    put("text", PROMPT.trimIndent())
                }
            )
        }
    )

    private fun createImageResponseItem(imageUrl: String): ResponseInputItem = ResponseInputItem(
        role = "user",
        content = buildJsonArray {
            add(
                buildJsonObject {
                    put("type", "input_image")
                    put(
                        "image_url",
                        imageUrl
                    )
                }
            )
        }
    )
}