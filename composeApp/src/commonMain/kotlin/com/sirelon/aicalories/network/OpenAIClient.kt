package com.sirelon.aicalories.network

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseInputItem
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.client.OpenAI
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OpenAIClient(
    private val openAI: OpenAI,
) {

    suspend fun test(): String? {
        val response = openAI.response(
            request = ResponseRequest(
                model = ModelId("gpt-4.1"),
                temperature = 0.2,
                maxOutputTokens = 300,

                input = ResponseInput(
                    items = listOf(
                        ResponseInputItem(
                            role = "system",
                            content = buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("type", "input_text")
                                        put(
                                            "text",
                                            """
Analyze item images and generate listing.

Return JSON with: title, description, price, category, condition.

Rules:
- Use only visible details
- Keep description short
- Combine all images if multiple
- Write all text in Ukrainian
""".trimIndent()
                                        )
                                    }
                                )
                            }
                        ),

                        ResponseInputItem(
                            role = "user",
                            content = buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("type", "input_image")
                                        put("image_url", "https://qosvjukxtnvtvarxnklv.supabase.co/storage/v1/object/public/test/JPG%20to%20WEBP%20temp%20image.webp")
                                    }
                                )
                            }
                        )
                    )
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

}