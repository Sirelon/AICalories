package com.sirelon.aicalories.network

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.client.OpenAI

class OpenAIClient(
    private val openAI: OpenAI,
) {

    suspend fun test(): String? {
        val response = openAI.response(
            request = ResponseRequest(
                model = ModelId("gpt-4.1"),
                input = ResponseInput("Write a haiku about Kotlin.")
            )
        )
        return response.outputText
    }

}