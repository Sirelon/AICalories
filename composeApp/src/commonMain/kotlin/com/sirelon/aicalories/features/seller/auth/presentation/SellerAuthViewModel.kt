package com.sirelon.aicalories.features.seller.auth.presentation

import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthRepository
import kotlinx.coroutines.launch

class SellerAuthViewModel(
    private val authRepository: OlxAuthRepository,
    private val apiClient: OlxApiClient,
) : BaseViewModel<SellerAuthContract.SellerAuthState, SellerAuthContract.SellerAuthEvent, SellerAuthContract.SellerAuthEffect>() {

    override fun initialState(): SellerAuthContract.SellerAuthState = SellerAuthContract.SellerAuthState()

    init {
        refreshSessionState()
    }

    override fun onEvent(event: SellerAuthContract.SellerAuthEvent) {
        when (event) {
            SellerAuthContract.SellerAuthEvent.ConnectClicked -> startAuthorization()
            SellerAuthContract.SellerAuthEvent.DisconnectClicked -> logout()
            SellerAuthContract.SellerAuthEvent.RefreshClicked -> refreshTokens()
            SellerAuthContract.SellerAuthEvent.TestMeClicked -> fetchAuthenticatedUser()
        }
    }

    fun onCallbackReceived(callbackUrl: String) {
        viewModelScope.launch {
            setState {
                it.copy(
                    status = SellerAuthContract.SellerAuthStatus.Processing,
                    statusMessage = "Exchanging OLX authorization code for tokens.",
                    errorMessage = null,
                )
            }

            authRepository.completeAuthorization(callbackUrl)
                .onSuccess { tokens ->
                    setState {
                        it.copy(
                            status = SellerAuthContract.SellerAuthStatus.Authorized,
                            isAuthorized = true,
                            statusMessage = "OLX account connected successfully.",
                            accessTokenExpiresAtEpochSeconds = tokens.expiresAtEpochSeconds,
                            errorMessage = null,
                        )
                    }
                    postEffect(SellerAuthContract.SellerAuthEffect.ShowMessage("OLX account connected."))
                }
                .onFailure { error ->
                    showError(error.message ?: "Failed to complete OLX authorization.")
                }
        }
    }

    private fun startAuthorization() {
        viewModelScope.launch {
            runCatching { authRepository.createAuthorizationRequest() }
                .onSuccess { request ->
                    setState {
                        it.copy(
                            status = SellerAuthContract.SellerAuthStatus.Processing,
                            statusMessage = "Opening OLX authorization in your browser.",
                            errorMessage = null,
                        )
                    }
                    postEffect(SellerAuthContract.SellerAuthEffect.LaunchBrowser(request.url))
                    setState {
                        it.copy(
                            status = SellerAuthContract.SellerAuthStatus.Processing,
                            statusMessage = "Waiting for OLX to return to the app.",
                        )
                    }
                }
                .onFailure { error ->
                    showError(error.message ?: "Failed to prepare OLX authorization.")
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            setState {
                SellerAuthContract.SellerAuthState(
                    statusMessage = "OLX session cleared.",
                )
            }
            postEffect(SellerAuthContract.SellerAuthEffect.ShowMessage("Disconnected from OLX."))
        }
    }

    private fun refreshTokens() {
        viewModelScope.launch {
            setState {
                it.copy(
                    status = SellerAuthContract.SellerAuthStatus.Processing,
                    statusMessage = "Refreshing OLX access token.",
                    errorMessage = null,
                )
            }

            authRepository.refreshIfNeeded(force = true)
                .onSuccess { tokens ->
                    setState {
                        it.copy(
                            status = SellerAuthContract.SellerAuthStatus.Authorized,
                            isAuthorized = true,
                            statusMessage = "OLX access token refreshed.",
                            accessTokenExpiresAtEpochSeconds = tokens.expiresAtEpochSeconds,
                            errorMessage = null,
                        )
                    }
                    postEffect(SellerAuthContract.SellerAuthEffect.ShowMessage("OLX token refreshed."))
                }
                .onFailure { error ->
                    showError(error.message ?: "Failed to refresh OLX token.")
                }
        }
    }

    private fun fetchAuthenticatedUser() {
        viewModelScope.launch {
            setState {
                it.copy(
                    status = SellerAuthContract.SellerAuthStatus.Processing,
                    statusMessage = "Loading OLX account profile.",
                    errorMessage = null,
                )
            }

            apiClient.getAuthenticatedUser()
                .onSuccess { user ->
                    val session = authRepository.currentSession()
                    setState {
                        it.copy(
                            status = SellerAuthContract.SellerAuthStatus.Authorized,
                            isAuthorized = true,
                            statusMessage = "OLX account is connected and API access works.",
                            accessTokenExpiresAtEpochSeconds = session.accessTokenExpiresAtEpochSeconds,
                            me = user,
                            errorMessage = null,
                        )
                    }
                    postEffect(SellerAuthContract.SellerAuthEffect.ShowMessage("Loaded OLX account profile."))
                }
                .onFailure { error ->
                    showError(error.message ?: "Failed to load OLX profile.")
                }
        }
    }

    private fun refreshSessionState() {
        viewModelScope.launch {
            val session = authRepository.currentSession()
            setState {
                it.copy(
                    status = if (session.isAuthorized) {
                        SellerAuthContract.SellerAuthStatus.Authorized
                    } else {
                        SellerAuthContract.SellerAuthStatus.Idle
                    },
                    isAuthorized = session.isAuthorized,
                    statusMessage = if (session.isAuthorized) {
                        "OLX account is already connected."
                    } else {
                        "Connect your OLX account to prepare seller API access."
                    },
                    accessTokenExpiresAtEpochSeconds = session.accessTokenExpiresAtEpochSeconds,
                    errorMessage = session.lastError,
                )
            }
        }
    }

    private fun showError(message: String) {
        viewModelScope.launch {
            val session = authRepository.currentSession()
            setState {
                it.copy(
                    status = SellerAuthContract.SellerAuthStatus.Error,
                    statusMessage = message,
                    errorMessage = message,
                    isAuthorized = session.isAuthorized,
                    accessTokenExpiresAtEpochSeconds = session.accessTokenExpiresAtEpochSeconds,
                )
            }
            postEffect(SellerAuthContract.SellerAuthEffect.ShowMessage(message))
        }
    }
}
