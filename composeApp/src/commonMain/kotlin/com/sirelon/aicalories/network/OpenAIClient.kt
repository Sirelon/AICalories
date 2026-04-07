package com.sirelon.aicalories.network

import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.Response
import com.aallam.openai.api.response.ResponseId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseInputItem
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.api.response.ResponseTool
import com.aallam.openai.client.OpenAI
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.data.GeneratedAdMapper
import com.sirelon.aicalories.network.responses.GeneratedAd
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private val DEFAULT_MODEL = ModelId("gpt-4.1")
private const val GENERATE_AD_TOOL_NAME = "generate_marketplace_listing"

private const val DEFAULT_IMAGE_DETAIL = "high"

private const val AD_GENERATION_INSTRUCTIONS = """
Create one OLX-style listing draft for the main item shown across the images.

Rules:
- Use only what is visible in the images and, if provided, explicitly stated in the seller note.
- If several different items appear, describe only the main item that is most central or repeated across the photos.
- Ignore background objects and unrelated items.
- You may use text that is clearly readable on the item, label, or box.
- Never invent brand, model, size, material, specifications, accessories, bundle contents, or defects.
- If identification is uncertain, use cautious generic wording.
- Write `title` and `description` in Ukrainian.
- `title`: concise, searchable, no hype, no marketing phrases.
- `description`: 2 to 4 short sentences, no bullet points, no markdown. Describe what the item is, visible condition, included visible parts, and visible wear or defects.
- Prices must be plain numbers only, with no currency symbols or extra text.
- Price for the second-hand marketplace value, not new retail price and not collectible premium price.
- Ensure `minPrice <= suggestedPrice <= maxPrice`.
- If the photos are not clear enough for a confident listing, return generic but usable text and conservative pricing.
"""

class OpenAIClient(
    private val openAI: OpenAI,
    private val json: Json,
) {

    private val mapper = GeneratedAdMapper()

    suspend fun analyzeThing(
        images: List<String>,
        sellerPrompt: String = "",
        model: ModelId = DEFAULT_MODEL,
        imageDetail: String = DEFAULT_IMAGE_DETAIL,
    ): Pair<ResponseId, Advertisement> {
        require(images.isNotEmpty()) { "At least one image is required to generate an advertisement." }
        require(model.id != "gpt-4") {
            "Legacy gpt-4 does not support image input or structured outputs for this flow. Use gpt-4.1, gpt-4o, or a newer model."
        }

        val response = openAI.response(
            request = ResponseRequest(
                model = model,
                instructions = AD_GENERATION_INSTRUCTIONS.trimIndent(),
                temperature = 0.1,
                maxOutputTokens = 200,
                parallelToolCalls = false,
                store = false,
                toolChoice = buildJsonObject {
                    put("type", "function")
                    put("name", GENERATE_AD_TOOL_NAME)
                },
                tools = listOf(
                    ResponseTool(
                        type = "function",
                        name = GENERATE_AD_TOOL_NAME,
                        description = "Generate a structured marketplace listing draft from seller photos.",
                        parameters = generatedAdParameters(),
                        strict = true,
                    )
                ),
                input = ResponseInput(
                    items = listOf(
                        createUserResponseItem(
                            images = images,
                            sellerPrompt = sellerPrompt,
                            imageDetail = imageDetail,
                        )
                    )
                ),
            )
        )

        val jsonString = extractJsonPayload(response)

        val generatedAd = json.decodeFromString<GeneratedAd>(jsonString)
        return response.id to mapper.mapToDomain(generatedAd, images)
    }

    private fun createUserResponseItem(
        images: List<String>,
        sellerPrompt: String,
        imageDetail: String,
    ): ResponseInputItem = ResponseInputItem(
        role = "user",
        content = buildJsonArray {
            add(createTextContent(buildUserPrompt(sellerPrompt)))
            images.forEach { imageUrl ->
                add(createImageContent(imageUrl, imageDetail))
            }
        }
    )

    private fun buildUserPrompt(sellerPrompt: String): String = buildString {
        append("Create one marketplace listing draft for the main item shown across all images.")

        sellerPrompt.trim()
            .takeIf { it.isNotEmpty() }
            ?.let {
                append("\nSeller note: ")
                append(it)
            }
    }

    private fun createTextContent(text: String) = buildJsonObject {
        put("type", "input_text")
        put("text", text)
    }

    private fun createImageContent(
        imageUrl: String,
        imageDetail: String,
    ) = buildJsonObject {
        put("type", "input_image")
        put("image_url", imageUrl)
        put("detail", imageDetail)
    }

    private fun generatedAdParameters(): Parameters = Parameters.buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("title") {
                put("type", "string")
                put("description", "Concise marketplace title in Ukrainian.")
                put("maxLength", 80)
            }
            putJsonObject("description") {
                put("type", "string")
                put("description", "Short marketplace description in Ukrainian.")
                put("maxLength", 500)
            }
            putJsonObject("suggestedPrice") {
                put("type", "number")
                put("minimum", 0)
            }
            putJsonObject("minPrice") {
                put("type", "number")
                put("minimum", 0)
            }
            putJsonObject("maxPrice") {
                put("type", "number")
                put("minimum", 0)
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("title"))
            add(JsonPrimitive("description"))
            add(JsonPrimitive("suggestedPrice"))
            add(JsonPrimitive("minPrice"))
            add(JsonPrimitive("maxPrice"))
        }
        put("additionalProperties", false)
    }

    private fun extractJsonPayload(response: Response): String {
        response.error?.message
            ?.takeIf { it.isNotBlank() }
            ?.let { message ->
                error("OpenAI request failed: $message")
            }

        if (response.status == "incomplete") {
            error("OpenAI returned an incomplete response: ${response.incompleteDetails ?: "no details"}")
        }

        response.output
            .asSequence()
            .flatMap { it.content.orEmpty().asSequence() }
            .mapNotNull { it.refusal }
            .firstOrNull()
            ?.let { refusal ->
                error("OpenAI refused to generate the advertisement: $refusal")
            }

        val functionArguments = response.output
            .asSequence()
            .firstOrNull { it.type == "function_call" && it.name == GENERATE_AD_TOOL_NAME }
            ?.arguments
            ?.trim()

        if (!functionArguments.isNullOrBlank()) {
            return sanitizeJsonPayload(functionArguments)
        }

        val payload = response.outputText
            ?: response.output
                .asSequence()
                .flatMap { it.content.orEmpty().asSequence() }
                .mapNotNull { it.text }
                .joinToString(separator = "\n")

        val sanitizedPayload = sanitizeJsonPayload(payload)
        if (sanitizedPayload.isBlank()) {
            error("OpenAI returned an empty advertisement payload.")
        }
        return sanitizedPayload
    }

    private fun sanitizeJsonPayload(payload: String): String {
        val trimmed = payload.trim()
        return when {
            trimmed.startsWith("```json") -> trimmed.removePrefix("```json").removeSuffix("```").trim()
            trimmed.startsWith("```") -> trimmed.removePrefix("```").removeSuffix("```").trim()
            else -> trimmed
        }
    }
}
