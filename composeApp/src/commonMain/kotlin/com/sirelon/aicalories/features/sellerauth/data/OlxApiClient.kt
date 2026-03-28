package com.sirelon.aicalories.features.sellerauth.data

import com.sirelon.aicalories.features.sellerauth.domain.OlxMeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class OlxApiClient(
    private val httpClient: HttpClient,
) {
    suspend fun getAuthenticatedUser(): Result<OlxMeResponse> = runCatching {
        val response = httpClient.get("${OlxConfig.apiBaseUrl}${OlxConfig.partnerApiBasePath}/users/me")
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }

        response.body()
    }
}
