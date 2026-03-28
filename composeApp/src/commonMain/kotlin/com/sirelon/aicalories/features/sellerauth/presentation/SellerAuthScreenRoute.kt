package com.sirelon.aicalories.features.sellerauth.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.features.sellerauth.data.OlxAuthCallbackBridge
import com.sirelon.aicalories.features.sellerauth.data.OlxExternalAuthLauncher
import com.sirelon.aicalories.features.sellerauth.domain.OlxLaunchResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SellerAuthScreenRoute() {
    val viewModel: SellerAuthViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    val launcher = remember(uriHandler) { ComposeUriOlxExternalAuthLauncher(uriHandler::openUri) }
    val callbackListener = remember(viewModel) { { url: String -> viewModel.onCallbackReceived(url) } }

    DisposableEffect(callbackListener) {
        OlxAuthCallbackBridge.listener = callbackListener
        onDispose {
            if (OlxAuthCallbackBridge.listener === callbackListener) {
                OlxAuthCallbackBridge.listener = null
            }
        }
    }

    LaunchedEffect(viewModel, launcher) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SellerAuthContract.SellerAuthEffect.LaunchBrowser -> {
                    when (val result = launcher.launch(effect.url)) {
                        OlxLaunchResult.Opened -> Unit
                        is OlxLaunchResult.Unsupported -> {
                            snackbarHostState.showSnackbar(result.reason)
                        }
                    }
                }

                is SellerAuthContract.SellerAuthEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    SellerAuthScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

private class ComposeUriOlxExternalAuthLauncher(
    private val openUri: (String) -> Unit,
) : OlxExternalAuthLauncher {
    override suspend fun launch(url: String): OlxLaunchResult {
        return runCatching {
            openUri(url)
            OlxLaunchResult.Opened
        }.getOrElse { error ->
            OlxLaunchResult.Unsupported(error.message ?: "This platform cannot open the OLX auth page yet.")
        }
    }
}
