package com.sirelon.aicalories.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val API_BASE_URL = "https://api.openai.com/v1"

class ApiTokenProvider {
    var token: String? = null
}

fun createHttpClient(tokenProvider: ApiTokenProvider): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                },
            )
        }
        install(Logging) {
            this.level = LogLevel.INFO
        }
        install(DefaultRequest) {
            url(API_BASE_URL)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            tokenProvider.token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }
