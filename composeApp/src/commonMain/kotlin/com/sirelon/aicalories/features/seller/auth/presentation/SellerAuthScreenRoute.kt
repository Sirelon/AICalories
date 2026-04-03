package com.sirelon.aicalories.features.seller.auth.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.features.seller.auth.data.OlxConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SellerAuthScreenRoute() {
    val viewModel: SellerAuthViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var webViewUrl by remember { mutableStateOf<String?>(null) }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is SellerAuthContract.SellerAuthEffect.LaunchBrowser -> {
                webViewUrl = effect.url
            }
            is SellerAuthContract.SellerAuthEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    SellerAuthScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )

    webViewUrl?.let { url ->
        Dialog(
            onDismissRequest = { webViewUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Connect OLX Account") },
                        navigationIcon = {
                            IconButton(onClick = { webViewUrl = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                    )
                },
            ) { paddingValues ->
                OlxAuthWebView(
                    url = url,
                    redirectUri = OlxConfig.redirectUri,
                    onUrlIntercepted = { callbackUrl ->
                        webViewUrl = null
                        viewModel.onCallbackReceived(callbackUrl)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}
