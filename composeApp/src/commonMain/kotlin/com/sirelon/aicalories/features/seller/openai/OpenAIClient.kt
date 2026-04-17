package com.sirelon.aicalories.features.seller.openai

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.Response
import com.aallam.openai.api.response.ResponseId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseInputItem
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.client.OpenAI
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.data.GeneratedAdMapper
import com.sirelon.aicalories.features.seller.categories.domain.AttributeInputType
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttribute
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttributeValue
import com.sirelon.aicalories.features.seller.openai.requests.OpenAIAttributeOptionRequest
import com.sirelon.aicalories.features.seller.openai.requests.OpenAIAttributeRequest
import com.sirelon.aicalories.features.seller.openai.requests.OpenAIAttributesRequest
import com.sirelon.aicalories.features.seller.openai.responses.OpenAIAttributeSuggestionResponse
import com.sirelon.aicalories.features.seller.openai.responses.OpenAIAttributeSuggestionsResponse
import com.sirelon.aicalories.features.seller.openai.responses.OpenAIGeneratedAd
import com.sirelon.aicalories.features.seller.openai.responses.OpenAIListingContextResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val DEFAULT_MODEL = ModelId("gpt-4.1")
private const val DEFAULT_IMAGE_DETAIL = "high"
private val NUMBER_PATTERN = Regex("""-?\d+(?:\.\d+)?""")
private const val LISTING_CONTEXT_INSTRUCTIONS = """
Extract structured listing context for one OLX Ukraine item.

Rules:
- This step extracts facts only. Do not write listing copy.
- Source priority:
  1. explicit seller facts
  2. readable text on the item, label, or box
  3. neutral visible details from the photos
- Keep exact seller-provided tokens for brand, model, size, purchase age, and condition when present.
- Do not infer season (`зимова`, `демісезонна`, `весняна`, `осіння`) unless the seller explicitly says it or the photos make it unmistakable.
- `itemType` must be a safe, broad item type in Ukrainian.
- `mustUsePhrases` should contain 0 to 5 short exact phrases copied from the seller note that are concrete and should be preserved in the listing or attributes.
- `visualFacts` should contain 2 to 6 short neutral visible facts from the photos.
- Use empty strings for unknown scalar fields and empty arrays when unknown.
- Return ONLY valid JSON with this exact shape:
  {"itemType":"string","sellerFacts":{"brand":"string","model":"string","size":"string","purchaseAge":"string","condition":"string","season":"string","material":"string"},"mustUsePhrases":["string"],"visualFacts":["string"]}
- The response must start with `{` and end with `}`.
"""

private const val AD_GENERATION_INSTRUCTIONS = """
Write one OLX Ukraine listing draft using the structured context JSON from the input.

Rules:
- Your job is to create a usable marketplace listing, not to write an image inspection report.
- Treat `sellerFacts` and `mustUsePhrases` as authoritative.
- If `sellerFacts.brand` is not empty, preserve that exact brand token in the title or description.
- If `sellerFacts.size` is not empty, preserve that exact size token in the title or description.
- If `sellerFacts.purchaseAge` is not empty, include it naturally in the description.
- Use `visualFacts` only to support the seller facts with neutral visible details.
- Do not infer season unless `sellerFacts.season` is not empty.
- Do not invent brand, size, material, season, accessories, defects, or condition.
- Do not add filler phrases like `стан видно на фото`, `додаткові питання в повідомленнях`, or `підійде для повсякденного носіння`.
- Write `title` and `description` in Ukrainian.
- `title`: short, searchable, specific. Prefer item type + brand + key detail + exact size when available.
- `description`: 3 to 5 short sentences, no bullet points, no markdown.
- Prices must be plain numbers only, with no currency symbols or extra text.
- All prices must be estimated in Ukrainian hryvnia (UAH) for the OLX Ukraine second-hand market.
- Price for the second-hand marketplace value, not new retail price and not collectible premium price.
- Ensure `minPrice <= suggestedPrice <= maxPrice`.
- Return ONLY valid JSON with this exact shape:
  {"title":"string","description":"string","suggestedPrice":number,"minPrice":number,"maxPrice":number}
- The response must start with `{` and end with `}`.
"""

private const val ATTRIBUTE_FILL_INSTRUCTIONS = """
Fill the provided OLX attributes for the same item.

Rules:
- This is form filling, not captioning.
- Source priority:
  1. structured seller facts from the previous response
  2. exact phrases from `mustUsePhrases`
  3. earlier image analysis context
  4. attribute labels and allowed option labels
- Return only attributes from the provided list.
- For select and multi-select attributes, return allowed option codes in `valueCodes`.
- For numeric and text attributes, return one plain value in `valueText`.
- If the previous structured context contains a brand, size, model, condition, purchase age, material, or season, use that first for matching attributes.
- Never replace an exact seller-provided value with an approximation. If the structured context says `XL`, do not return `L-XL`, `L`, or leave it empty.
- If there is a brand attribute and the structured context contains `Nike`, fill that brand from the structured context.
- If there is a size attribute and the structured context contains `XL`, fill that size from the structured context.
- If a structured value matches an allowed option label semantically, return the corresponding option code.
- Prefer exact option codes, but if needed you may repeat the matched label in `valueText` as a fallback hint.
- If the structured context does not answer the field and the value is not directly visible or clearly inferable, leave `valueCodes` empty and `valueText` empty.
- Never invent unsupported details.
- Respect numeric limits.
- Use at most one value unless the attribute explicitly supports multiple choices.
- Return ONLY valid JSON with this exact shape:
  {"attributes":[{"code":"string","valueCodes":["string"],"valueText":"string","confidence":"high|medium|low"}]}
- The response must start with `{` and end with `}`.
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

    suspend fun fillAdditionalInfo(
        previousResponseId: ResponseId,
        attributes: List<OlxAttribute>,
        sellerPrompt: String,
        model: ModelId = DEFAULT_MODEL,
    ): Map<String, List<OlxAttributeValue>> {
        if (attributes.isEmpty()) return emptyMap()

        val response = openAI.response(
            request = ResponseRequest(
                // Vision-capable model reused for the follow-up attribute pass.
                model = model,
                // Pulls server-side context from the stored listing-analysis response.
                previousResponseId = previousResponseId,
                // Re-states task rules for this turn; previous response context does not replace instructions.
                instructions = ATTRIBUTE_FILL_INSTRUCTIONS.trimIndent(),
                // Deterministic output is preferable here because we want stable attribute picks.
                temperature = 0.0,
                // Attribute-filling output grows with the number of fields/options, so cap proportionally.
                maxOutputTokens = attributeOutputTokenLimit(attributes.size),
                // This follow-up response is not chained further yet.
                store = false,
                // Use separate text items so seller facts and attribute schema stay explicit.
                input = ResponseInput(
                    items = buildList {
                        sellerPrompt.trim()
                            .takeIf { it.isNotEmpty() }
                            ?.let { prompt ->
                                add(createTextUserResponseItem(buildAttributeSellerNotePrompt(prompt)))
                            }
                        add(createTextUserResponseItem(buildAttributeFillPrompt(attributes)))
                    }
                ),
            )
        )

        val jsonString = extractTextPayload(response)
        val suggestions = json.decodeFromString<OpenAIAttributeSuggestionsResponse>(jsonString)
        return mapAttributeSuggestions(attributes, suggestions)
    }

    suspend fun analyzeThing(
        images: List<String>,
        sellerPrompt: String,
        model: ModelId = DEFAULT_MODEL,
        imageDetail: String = DEFAULT_IMAGE_DETAIL,
    ): Pair<ResponseId, Advertisement> {
        require(images.isNotEmpty()) { "At least one image is required to generate an advertisement." }
        require(model.id != "gpt-4") {
            "Legacy gpt-4 does not support image input or structured outputs for this flow. Use gpt-4.1, gpt-4o, or a newer model."
        }

        val contextResponse = openAI.response(
            request = ResponseRequest(
                // Vision-capable model for structured seller/visual fact extraction.
                model = model,
                // Extract normalized listing context from seller note + photos.
                instructions = LISTING_CONTEXT_INSTRUCTIONS.trimIndent(),
                // Deterministic extraction keeps seller facts stable.
                temperature = 0.0,
                // Structured fact output is compact but slightly larger than the final ad.
                maxOutputTokens = 250,
                // Stored so the draft and attribute-fill steps can reference the structured context.
                store = true,
                // One multimodal user turn: extraction task + seller note + images.
                input = ResponseInput(
                    items = listOf(
                        createContextUserResponseItem(
                            images = images,
                            sellerPrompt = sellerPrompt,
                            imageDetail = imageDetail,
                        )
                    )
                ),
            )
        )

        val contextJson = extractTextPayload(contextResponse)
        val analysisContext = json.decodeFromString<OpenAIListingContextResponse>(contextJson)

        val listingResponse = openAI.response(
            request = ResponseRequest(
                // Same model drafts the final listing from the structured context.
                model = model,
                // Seller facts are already normalized in the first step; this pass focuses on wording.
                instructions = AD_GENERATION_INSTRUCTIONS.trimIndent(),
                // Keep wording stable and avoid stylistic drift.
                temperature = 0.1,
                maxOutputTokens = 200,
                // Store the final listing response so follow-up attribute fill can reference the full chain.
                store = true,
                previousResponseId = contextResponse.id,
                input = ResponseInput(
                    items = listOf(
                        createTextUserResponseItem(buildListingDraftPrompt(analysisContext))
                    )
                ),
            )
        )

        val listingJson = extractTextPayload(listingResponse)
        val generatedAd = json.decodeFromString<OpenAIGeneratedAd>(listingJson)
        return listingResponse.id to mapper.mapToDomain(generatedAd, images)
    }

    private fun createContextUserResponseItem(
        images: List<String>,
        sellerPrompt: String,
        imageDetail: String,
    ): ResponseInputItem = ResponseInputItem(
        role = "user",
        content = buildJsonArray {
            add(createTextContent(buildContextPrompt()))
            sellerPrompt.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { prompt ->
                    add(createTextContent(buildSellerNotePrompt(prompt)))
                }
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

    private fun buildContextPrompt(): String =
        "Extract structured listing context for the main item shown in the photos."

    private fun buildListingDraftPrompt(analysisContext: OpenAIListingContextResponse): String = buildString {
        appendLine("Structured listing context JSON:")
        append(compactJson.encodeToString(analysisContext))
    }

    private fun buildSellerNotePrompt(sellerPrompt: String): String = buildString {
        appendLine("Seller facts from the user. Treat them as authoritative listing facts.")
        appendLine("Preserve exact brand, size, model, condition, and purchase-age wording when relevant.")
        appendLine("Do not replace exact seller facts with guesses or broader ranges.")
        appendLine("Seller note:")
        appendLine("<<<")
        appendLine(sellerPrompt)
        append(">>>")
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
        // "high" gives better recognition when later turns depend on precise item understanding.
        put("detail", imageDetail)
    }

    private fun buildAttributeSellerNotePrompt(sellerPrompt: String): String = buildString {
        appendLine("Seller facts to use first when filling attributes.")
        appendLine("Preserve exact seller-provided brand, size, model, condition, and purchase-age values.")
        appendLine("Seller note:")
        appendLine("<<<")
        appendLine(sellerPrompt)
        append(">>>")
    }

    private fun buildAttributeFillPrompt(attributes: List<OlxAttribute>): String = buildString {
        appendLine("Fill these OLX attributes for the same item.")
        appendLine("Available OLX attributes and allowed options:")
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

    private fun extractTextPayload(response: Response): String {
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
        suggestion: OpenAIAttributeSuggestionResponse,
    ): List<OlxAttributeValue> = when (attribute.inputType) {
        AttributeInputType.SingleSelect -> resolveSuggestedOptionValues(attribute, suggestion)
            .take(1)

        AttributeInputType.MultiSelect -> resolveSuggestedOptionValues(attribute, suggestion)

        AttributeInputType.NumericInput -> extractNumericValue(suggestion.valueText)
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

    private fun resolveSuggestedOptionValues(
        attribute: OlxAttribute,
        suggestion: OpenAIAttributeSuggestionResponse,
    ): List<OlxAttributeValue> = buildList {
        suggestion.valueCodes
            .orEmpty()
            .forEach { candidate ->
                resolveAllowedValue(attribute, candidate)?.let(::add)
            }

        suggestion.valueText
            ?.takeIf { it.isNotBlank() }
            ?.let(::splitSuggestedValues)
            .orEmpty()
            .forEach { candidate ->
                resolveAllowedValue(attribute, candidate)?.let(::add)
            }
    }.distinctBy { it.code }

    private fun resolveAllowedValue(
        attribute: OlxAttribute,
        candidate: String,
    ): OlxAttributeValue? {
        val normalizedCandidate = normalizeForMatching(candidate)
        if (normalizedCandidate.isEmpty()) return null

        return attribute.allowedValues.firstOrNull { value ->
            val normalizedCode = normalizeForMatching(value.code)
            val normalizedLabel = normalizeForMatching(value.label)

            normalizedCandidate == normalizedCode ||
                normalizedCandidate == normalizedLabel ||
                normalizedCandidate.contains(normalizedCode) ||
                normalizedCandidate.contains(normalizedLabel) ||
                normalizedCode.contains(normalizedCandidate) ||
                normalizedLabel.contains(normalizedCandidate)
        }
    }

    private fun splitSuggestedValues(valueText: String): List<String> = valueText
        .split(",", ";", "/", "\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .ifEmpty { listOf(valueText.trim()) }

    private fun extractNumericValue(valueText: String?): String? = valueText
        ?.replace(',', '.')
        ?.let { numericText ->
            NUMBER_PATTERN.find(numericText)?.value
        }
        ?.takeIf { it.isNotBlank() }

    private fun normalizeForMatching(value: String): String = buildString {
        value.lowercase().forEach { char ->
            if (char.isLetterOrDigit()) append(char)
        }
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
            trimmed.startsWith("```json") -> trimmed.removePrefix("```json").removeSuffix("```").trim()
            trimmed.startsWith("```") -> trimmed.removePrefix("```").removeSuffix("```").trim()
            else -> trimmed
        }
    }
}
