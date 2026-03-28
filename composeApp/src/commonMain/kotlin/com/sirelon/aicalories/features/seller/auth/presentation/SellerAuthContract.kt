package com.sirelon.aicalories.features.seller.auth.presentation

import com.sirelon.aicalories.features.seller.auth.domain.OlxMeResponse

interface SellerAuthContract {

    data class SellerAuthState(
        val status: SellerAuthStatus = SellerAuthStatus.Idle,
        val isAuthorized: Boolean = false,
        val statusMessage: String = "Connect your OLX account to prepare seller API access.",
        val accessTokenExpiresAtEpochSeconds: Long? = null,
        val me: OlxMeResponse? = null,
        val errorMessage: String? = null,
    ) {
        val isBusy: Boolean
            get() = status == SellerAuthStatus.Processing

        val statusLabel: String
            get() = when (status) {
                SellerAuthStatus.Idle -> "Not connected"
                SellerAuthStatus.Processing -> "Processing"
                SellerAuthStatus.Authorized -> "OLX account connected"
                SellerAuthStatus.Error -> "Authorization error"
            }
    }

    enum class SellerAuthStatus {
        Idle,
        Processing,
        Authorized,
        Error,
    }

    sealed interface SellerAuthEvent {
        data object ConnectClicked : SellerAuthEvent
        data object DisconnectClicked : SellerAuthEvent
        data object RefreshClicked : SellerAuthEvent
        data object TestMeClicked : SellerAuthEvent
    }

    sealed interface SellerAuthEffect {
        data class LaunchBrowser(val url: String) : SellerAuthEffect
        data class ShowMessage(val message: String) : SellerAuthEffect
    }
}
