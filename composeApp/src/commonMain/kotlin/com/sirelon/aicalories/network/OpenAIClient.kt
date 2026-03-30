package com.sirelon.aicalories.network

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseInputItem
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.client.OpenAI
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


private const val PROMPT =
    """Analyze the provided images of a single item and generate a marketplace listing.

Return ONLY valid JSON:
{
  "title": "...",
  "description": "...",
  "price": number,
  "category": "...",
  "condition": "new|like new|good|fair|poor"
}

Rules:
- Use only visible details
- Do not invent brand or model
- Keep description short (2–3 sentences)
- Combine details from all images
- Write all text in Ukrainian
- No text outside JSON"""

class OpenAIClient(
    private val openAI: OpenAI,
) {

    suspend fun analyzeThing(images: List<String>): String {
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

        return response.outputText ?: response
            .output
            .joinToString(separator = "\n") {
                it.content
                    .orEmpty()
                    .joinToString(separator = "\n") {
                        it.text.orEmpty()
                    }
            }
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