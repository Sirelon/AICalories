package com.sirelon.aicalories.features.seller.auth.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sirelon.aicalories.features.seller.auth.data.OlxConfig
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.continue_with_olx
import com.sirelon.aicalories.generated.resources.ic_x
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OlxAuthDialogScreen(
    url: String,
    onDismiss: () -> Unit,
    onCallbackReceived: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.continue_with_olx)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(Res.drawable.ic_x), contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        OlxAuthWebView(
            url = url,
            redirectUri = OlxConfig.redirectUri,
            onUrlIntercepted = onCallbackReceived,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}
