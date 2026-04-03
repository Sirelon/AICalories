package com.sirelon.aicalories.features.auth.data

import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthRepository
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthSessionStore
import com.sirelon.aicalories.features.seller.auth.data.OlxCredentialsProvider
import com.sirelon.aicalories.features.seller.auth.data.OlxRedirectHandler
import com.sirelon.aicalories.features.seller.auth.data.OlxTokenStore
import com.sirelon.aicalories.features.seller.auth.data.createOlxAuthorizedHttpClient
import com.sirelon.aicalories.features.seller.auth.data.createOlxHttpClient
import com.sirelon.aicalories.features.seller.auth.domain.OlxAuthCallback
import com.sirelon.aicalories.features.seller.auth.domain.OlxTokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class OlxApiClientTest {

    @Test
    fun `getAuthenticatedUser attaches bearer token and version header`() = runBlocking {
        var authorizationHeader: String? = null
        var versionHeader: String? = null
        val tokenStore = OlxTokenStore(InMemoryOlxKeyValueStore()).apply {
            write(
                OlxTokens(
                    accessToken = "active-token",
                    refreshToken = "refresh-token",
                    expiresInSeconds = 86_400,
                    tokenType = "bearer",
                    scope = "v2 read write",
                    issuedAtEpochSeconds = 4_102_444_800,
                ),
            )
        }
        val holder = createRepository(
            tokenStore = tokenStore,
            engine = MockEngine { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                versionHeader = request.headers["Version"]
                respond(
                    content = """
                        {
                          "id": 77,
                          "email": "seller@example.com",
                          "name": "Seller"
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )
        val apiClient = OlxApiClient(
            createOlxAuthorizedHttpClient(
                authRefreshClient = createOlxHttpClient(holder.engine),
                credentialsProvider = TestCredentialsProvider(),
                tokenStore = tokenStore,
                engine = holder.engine,
            ),
        )
        val result = apiClient.getAuthenticatedUser()

        assertTrue(result.isSuccess)
        assertEquals("Bearer active-token", authorizationHeader)
        assertEquals("2.0", versionHeader)
    }

    @Test
    fun `getAuthenticatedUser refreshes and retries after invalid token response`() = runBlocking {
        var userRequestCount = 0
        var seenAuthorizationHeaders = mutableListOf<String?>()
        val engine = MockEngine { request ->
            when {
                request.url.toString().contains("/partner/users/me") -> {
                    userRequestCount += 1
                    seenAuthorizationHeaders += request.headers[HttpHeaders.Authorization]
                    if (userRequestCount == 1) {
                        respond(
                            content = """
                                {
                                  "error": "invalid_token",
                                  "error_description": "The access token provided is invalid"
                                }
                            """.trimIndent(),
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    } else {
                        respond(
                            content = """
                                {
                                  "id": 88,
                                  "email": "seller@example.com",
                                  "name": "Seller"
                                }
                            """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }
                }

                request.url.toString().contains("/open/oauth/token") -> {
                    respond(
                        content = """
                            {
                              "access_token": "refreshed-token",
                              "refresh_token": "new-refresh-token",
                              "expires_in": 86400,
                              "token_type": "bearer",
                              "scope": "v2 read write"
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                else -> error("Unexpected request: ${request.url}")
            }
        }
        val tokenStore = OlxTokenStore(InMemoryOlxKeyValueStore()).apply {
            write(
                OlxTokens(
                    accessToken = "stale-token",
                    refreshToken = "refresh-token",
                    expiresInSeconds = 86_400,
                    tokenType = "bearer",
                    scope = "v2 read write",
                    issuedAtEpochSeconds = 4_102_444_800,
                ),
            )
        }
        val holder = createRepository(tokenStore = tokenStore, engine = engine)
        val apiClient = OlxApiClient(
            createOlxAuthorizedHttpClient(
                authRefreshClient = createOlxHttpClient(engine),
                credentialsProvider = TestCredentialsProvider(),
                tokenStore = tokenStore,
                engine = engine,
            ),
        )

        val result = apiClient.getAuthenticatedUser()

        assertTrue(result.isSuccess)
        assertEquals(listOf("Bearer stale-token", "Bearer refreshed-token"), seenAuthorizationHeaders)
        assertEquals("refreshed-token", tokenStore.read()?.accessToken)
    }

    @Test
    fun `getAuthenticatedUser clears persisted tokens when refresh returns invalid_grant`() = runBlocking {
        var refreshRequestCount = 0
        val engine = MockEngine { request ->
            when {
                request.url.toString().contains("/partner/users/me") -> {
                    respond(
                        content = """
                            {
                              "error": "invalid_token",
                              "error_description": "The access token provided is invalid"
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                request.url.toString().contains("/open/oauth/token") -> {
                    refreshRequestCount += 1
                    respond(
                        content = """
                            {
                              "error": "invalid_grant",
                              "error_description": "Refresh token expired"
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                else -> error("Unexpected request: ${request.url}")
            }
        }
        val tokenStore = OlxTokenStore(InMemoryOlxKeyValueStore()).apply {
            write(
                OlxTokens(
                    accessToken = "stale-token",
                    refreshToken = "bad-refresh-token",
                    expiresInSeconds = 86_400,
                    tokenType = "bearer",
                    scope = "v2 read write",
                    issuedAtEpochSeconds = 4_102_444_800,
                ),
            )
        }
        val apiClient = OlxApiClient(
            createOlxAuthorizedHttpClient(
                authRefreshClient = createOlxHttpClient(engine),
                credentialsProvider = TestCredentialsProvider(),
                tokenStore = tokenStore,
                engine = engine,
            ),
        )

        assertFailsWith<OlxApiException> {
            apiClient.getAuthenticatedUser().getOrThrow()
        }
        assertEquals(1, refreshRequestCount)
        assertNull(tokenStore.read())
    }

    private fun createRepository(
        tokenStore: OlxTokenStore,
        engine: MockEngine,
    ): TestRepositoryHolder {
        return TestRepositoryHolder(
            engine = engine,
            repository = OlxAuthRepository(
                httpClient = createOlxHttpClient(engine),
                credentialsProvider = TestCredentialsProvider(),
                tokenStore = tokenStore,
                authSessionStore = OlxAuthSessionStore(InMemoryOlxKeyValueStore()),
                redirectHandler = TestRedirectHandler(),
            ),
        )
    }

    private data class TestRepositoryHolder(
        val engine: MockEngine,
        val repository: OlxAuthRepository,
    )

    private class TestCredentialsProvider : OlxCredentialsProvider {
        override suspend fun getClientId(): String = "test-client-id"

        override suspend fun getClientSecret(): String = "test-client-secret"
    }

    private class TestRedirectHandler : OlxRedirectHandler {
        override fun buildRedirectUri(platform: com.sirelon.aicalories.platform.PlatformTargets): String {
            return "selolxai://olx-auth/callback"
        }

        override fun parseCallback(url: String): OlxAuthCallback {
            val parsed = io.ktor.http.Url(url)
            return OlxAuthCallback(
                code = parsed.parameters["code"],
                state = parsed.parameters["state"],
                error = parsed.parameters["error"],
                errorDescription = parsed.parameters["error_description"],
            )
        }
    }
}
