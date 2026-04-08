package com.sirelon.aicalories.features.seller.openai

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
import com.sirelon.aicalories.features.seller.categories.domain.AttributeInputType
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttribute
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttributeValue
import com.sirelon.aicalories.features.seller.openai.requests.OpenAIAttributeOptionRequest
import com.sirelon.aicalories.features.seller.openai.requests.OpenAIAttributeRequest
import com.sirelon.aicalories.features.seller.openai.requests.OpenAIAttributesRequest
import com.sirelon.aicalories.features.seller.openai.responses.OpenAIAttributeSuggestionsResponse
import com.sirelon.aicalories.features.seller.openai.responses.OpenAIGeneratedAd
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private val DEFAULT_MODEL = ModelId("gpt-4.1")
private const val GENERATE_AD_TOOL_NAME = "generate_marketplace_listing"
private const val FILL_ATTRIBUTES_TOOL_NAME = "fill_marketplace_attributes"

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

private const val ATTRIBUTE_FILL_INSTRUCTIONS = """
Fill the provided OLX attributes for the previously analyzed item.

Rules:
- Use only the earlier item analysis context and the provided attribute list.
- Return only attributes from the provided list.
- For select and multi-select attributes, return allowed option codes in `valueCodes`.
- For numeric and text attributes, return one plain value in `valueText`.
- If a value is uncertain, not visible, or not inferable, leave `valueCodes` empty and `valueText` empty.
- Never invent hidden specifications or unsupported details.
- Respect numeric limits.
- Use at most one value unless the attribute explicitly supports multiple choices.
"""

class OpenAIClient(
    private val openAI: OpenAI,
    private val json: Json,
) {

    private val mapper = GeneratedAdMapper()
    // Compact serializer for prompt-side payloads so we don't waste tokens on nulls/defaults.
    private val compactJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }

    // Temporary no-op overload so the in-progress attribute pipeline can compile
    // before it is wired to `fillAdditionalInfo(...)`.
    suspend fun analyzeThing() = Unit

    suspend fun fillAdditionalInfo(
        previousResponseId: ResponseId,
        attributes: List<OlxAttribute>,
        model: ModelId = DEFAULT_MODEL,
    ): Map<String, List<OlxAttributeValue>> {
        if (attributes.isEmpty()) return emptyMap()

        val response = openAI.response(
            request = ResponseRequest(
                model = model,
                // Reuses the model's prior understanding of the item from the image-analysis turn.
                previousResponseId = previousResponseId,
                instructions = ATTRIBUTE_FILL_INSTRUCTIONS.trimIndent(),
                temperature = 0.0,
                maxOutputTokens = attributeOutputTokenLimit(attributes.size),
                parallelToolCalls = false,
                store = false,
                // Force a single structured function payload instead of free-form text.
                toolChoice = buildJsonObject {
                    put("type", "function")
                    put("name", FILL_ATTRIBUTES_TOOL_NAME)
                },
                tools = listOf(
                    ResponseTool(
                        type = "function",
                        name = FILL_ATTRIBUTES_TOOL_NAME,
                        description = "Fill OLX attribute values for the previously analyzed marketplace item.",
                        parameters = generatedAttributeSuggestionParameters(),
                        strict = true,
                    )
                ),
                input = ResponseInput(
                    items = listOf(
                        createTextUserResponseItem(buildAttributeFillPrompt(attributes))
                    )
                ),
            )
        )

        val jsonString = extractToolArguments(response, FILL_ATTRIBUTES_TOOL_NAME)
        val suggestions = json.decodeFromString<OpenAIAttributeSuggestionsResponse>(jsonString)
        return mapAttributeSuggestions(attributes, suggestions)
    }

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

        val jsonString = extractToolArguments(response, GENERATE_AD_TOOL_NAME)

        val generatedAd = json.decodeFromString<OpenAIGeneratedAd>(jsonString)
        return response.id to mapper.mapToDomain(generatedAd, images)
    }

    private fun createUserResponseItem(
        images: List<String>,
        sellerPrompt: String,
        imageDetail: String,
    ): ResponseInputItem = ResponseInputItem(
        role = "user",
        content = buildJsonArray {
            add(createTextContent(buildAdPrompt(sellerPrompt)))
            images.forEach { imageUrl ->
                add(createImageContent(imageUrl, imageDetail))
            }
        }
    )

    private fun createTextUserResponseItem(text: String): ResponseInputItem = ResponseInputItem(
        role = "user",
        content = buildJsonArray {
            add(createTextContent(text))
        }
    )

    private fun buildAdPrompt(sellerPrompt: String): String = buildString {
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
        // "high" gives better recognition for attribute extraction; lower detail is cheaper but weaker.
        put("detail", imageDetail)
    }

    private fun buildAttributeFillPrompt(attributes: List<OlxAttribute>): String = buildString {
        appendLine("Fill these OLX attributes for the previously analyzed item.")
        // Send a compact, model-friendly schema instead of the raw OLX transport payload.
        append("Attributes JSON: ")
        append(compactJson.encodeToString(OpenAIAttributesRequest(attributes.map(::toAttributeRequest))))
    }

    private fun toAttributeRequest(attribute: OlxAttribute): OpenAIAttributeRequest =
        OpenAIAttributeRequest(
            code = attribute.code,
            label = attribute.label,
            type = attribute.inputType.toOpenAIType(),
            required = true.takeIf { attribute.validationRules.required },
            options = attribute.allowedValues
                .takeIf { it.isNotEmpty() }
                ?.map { value ->
                    OpenAIAttributeOptionRequest(
                        code = value.code,
                        label = value.label,
                    )
                },
            min = attribute.validationRules.min,
            max = attribute.validationRules.max,
            unit = attribute.unit.takeIf { it.isNotBlank() },
        )

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

    private fun generatedAttributeSuggestionParameters(): Parameters = Parameters.buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("attributes") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("code") {
                            put("type", "string")
                        }
                        putJsonObject("valueCodes") {
                            put("type", "array")
                            putJsonObject("items") {
                                put("type", "string")
                            }
                        }
                        putJsonObject("valueText") {
                            put("type", "string")
                        }
                        putJsonObject("confidence") {
                            put("type", "string")
                            putJsonArray("enum") {
                                add(JsonPrimitive("high"))
                                add(JsonPrimitive("medium"))
                                add(JsonPrimitive("low"))
                            }
                        }
                    }
                    putJsonArray("required") {
                        add(JsonPrimitive("code"))
                        add(JsonPrimitive("valueCodes"))
                        add(JsonPrimitive("valueText"))
                        add(JsonPrimitive("confidence"))
                    }
                    put("additionalProperties", false)
                }
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("attributes"))
        }
        put("additionalProperties", false)
    }

    private fun extractToolArguments(
        response: Response,
        toolName: String,
    ): String {
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
            // Responses API returns strict tool output here as JSON arguments.
            .firstOrNull { it.type == "function_call" && it.name == toolName }
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

    private fun attributeOutputTokenLimit(attributeCount: Int): Int =
        // Attribute filling scales with the number of fields/options, so keep the cap proportional.
        (attributeCount * 40).coerceIn(200, 1200)

    private fun mapAttributeSuggestions(
        attributes: List<OlxAttribute>,
        suggestions: OpenAIAttributeSuggestionsResponse,
    ): Map<String, List<OlxAttributeValue>> {
        val attributesByCode = attributes.associateBy { it.code }

        return suggestions.attributes
            .orEmpty()
            .mapNotNull { suggestion ->
                val code = suggestion.code ?: return@mapNotNull null
                val attribute = attributesByCode[code] ?: return@mapNotNull null
                code to mapSuggestedValues(attribute, suggestion)
            }
            .toMap()
    }

    private fun mapSuggestedValues(
        attribute: OlxAttribute,
        suggestion: com.sirelon.aicalories.features.seller.openai.responses.OpenAIAttributeSuggestionResponse,
    ): List<OlxAttributeValue> = when (attribute.inputType) {
        AttributeInputType.SingleSelect -> suggestion.valueCodes
            .orEmpty()
            .take(1)
            .mapNotNull { code -> attribute.allowedValues.find { it.code == code } }

        AttributeInputType.MultiSelect -> suggestion.valueCodes
            .orEmpty()
            .distinct()
            .mapNotNull { code -> attribute.allowedValues.find { it.code == code } }

        AttributeInputType.NumericInput -> suggestion.valueText
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { value ->
                val number = value.toDoubleOrNull() ?: return@takeIf false
                val min = attribute.validationRules.min
                val max = attribute.validationRules.max
                (min == null || number >= min) && (max == null || number <= max)
            }
            ?.let { listOf(OlxAttributeValue(code = attribute.code, label = it)) }
            .orEmpty()

        AttributeInputType.TextInput -> suggestion.valueText
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { listOf(OlxAttributeValue(code = attribute.code, label = it)) }
            .orEmpty()
    }

    private fun AttributeInputType.toOpenAIType(): String = when (this) {
        AttributeInputType.SingleSelect -> "single_select"
        AttributeInputType.MultiSelect -> "multi_select"
        AttributeInputType.NumericInput -> "number"
        AttributeInputType.TextInput -> "text"
    }

    private fun sanitizeJsonPayload(payload: String): String {
        val trimmed = payload.trim()
        return when {
            trimmed.startsWith("```json") -> trimmed.removePrefix("```json").removeSuffix("```")
                .trim()

            trimmed.startsWith("```") -> trimmed.removePrefix("```").removeSuffix("```").trim()
            else -> trimmed
        }
    }
}
