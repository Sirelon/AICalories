package com.sirelon.aicalories.features.seller.auth.data

import com.sirelon.aicalories.features.seller.auth.domain.OlxApiError
import com.sirelon.aicalories.features.seller.auth.domain.OlxApiException
import com.sirelon.aicalories.features.seller.auth.domain.OlxTokens
import com.sirelon.aicalories.supabase.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

fun createOlxHttpClient(engine: HttpClientEngine? = null): HttpClient {
    return if (engine != null) {
        HttpClient(engine, commonOlxHttpClientConfig())
    } else {
        HttpClient(commonOlxHttpClientConfig())
    }
}

fun createOlxAuthorizedHttpClient(
    authRefreshClient: HttpClient,
    credentialsProvider: OlxCredentialsProvider,
    tokenStore: OlxTokenStore,
    engine: HttpClientEngine? = null,
): HttpClient {
    val configure: HttpClientConfig<*>.() -> Unit = {
        commonOlxHttpClientConfig().invoke(this)
        install(Auth) {
            bearer {
                loadTokens {
                    tokenStore.read()?.toBearerTokens()
                }
                refreshTokens {
                    try {
                        val refreshedTokens = refreshOlxBearerTokens(
                            client = authRefreshClient,
                            credentialsProvider = credentialsProvider,
                            refreshToken = oldTokens?.refreshToken,
                        )
                        if (refreshedTokens == null) {
                            tokenStore.clear()
                            null
                        } else {
                            tokenStore.write(refreshedTokens)
                            refreshedTokens.toBearerTokens()
                        }
                    } catch (exception: OlxApiException) {
                        if (exception.error.isTerminalRefreshFailure()) {
                            tokenStore.clear()
                        }
                        throw exception
                    }
                }
            }
        }
    }

    return if (engine != null) {
        HttpClient(engine, configure)
    } else {
        HttpClient(configure)
    }
}

private fun commonOlxHttpClientConfig(): HttpClientConfig<*>.() -> Unit = {
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
        level = LogLevel.INFO
    }
    install(DefaultRequest) {
        header(HttpHeaders.Accept, ContentType.Application.Json)
        header("Version", OlxConfig.apiVersion)
    }
    expectSuccess = false
    defaultRequest {
        url(SupabaseConfig.OLX_API_BASE_URL)
    }
}

private suspend fun refreshOlxBearerTokens(
    client: HttpClient,
    credentialsProvider: OlxCredentialsProvider,
    refreshToken: String?,
): OlxTokens? {
    if (refreshToken.isNullOrBlank()) return null

    val response = client.post("/api/${OlxConfig.authTokenPath}") {
        contentType(ContentType.Application.Json)
        setBody(
            RefreshTokenRequest(
                grantType = "refresh_token",
                clientId = credentialsProvider.getClientId(),
                clientSecret = credentialsProvider.getClientSecret(),
                refreshToken = refreshToken,
            ),
        )
    }

    if (!response.status.isSuccess()) {
        throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
    }

    return response.body<RefreshTokenResponse>().toDomain()
}

private fun OlxTokens.toBearerTokens(): BearerTokens = BearerTokens(
    accessToken = accessToken,
    refreshToken = refreshToken ?: "",
)

private fun OlxApiError.isTerminalRefreshFailure(): Boolean = when (this) {
    is OlxApiError.InvalidGrant, is OlxApiError.InvalidToken -> true
    else -> false
}

@Serializable
private class RefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresInSeconds: Long,
    @SerialName("token_type") val tokenType: String,
    @SerialName("scope") val scope: String,
) {
    fun toDomain(): OlxTokens = OlxTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresInSeconds = expiresInSeconds,
        tokenType = tokenType,
        scope = scope,
        issuedAtEpochSeconds = Clock.System.now().toEpochMilliseconds() / 1000,
    )
}

@Serializable
private class RefreshTokenRequest(
    @SerialName("grant_type") val grantType: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("client_secret") val clientSecret: String,
    @SerialName("refresh_token") val refreshToken: String,
)
