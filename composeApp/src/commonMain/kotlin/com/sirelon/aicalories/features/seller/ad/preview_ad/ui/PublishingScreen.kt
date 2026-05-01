package com.sirelon.aicalories.features.seller.ad.preview_ad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.PulsingCircles
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdViewModel
import com.sirelon.aicalories.features.seller.ad.publish_success.PublishSuccessData
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ic_share_2
import com.sirelon.aicalories.generated.resources.publishing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PublishingScreen(
    viewModel: PreviewAdViewModel,
    onPublishSuccess: (PublishSuccessData) -> Unit,
    onPublishFinishedWithoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var handledSuccess by remember { mutableStateOf(false) }
    var handledFailure by remember { mutableStateOf(false) }

    LaunchedEffect(state.publishSuccessData) {
        val successData = state.publishSuccessData
        if (successData != null && !handledSuccess) {
            handledSuccess = true
            onPublishSuccess(successData)
        }
    }

    LaunchedEffect(state.publishFailureMessage) {
        val failureMessage = state.publishFailureMessage
        if (failureMessage != null && !handledFailure) {
            handledFailure = true
            snackbarHostState.showSnackbar(failureMessage)
            onPublishFinishedWithoutSuccess()
        }
    }

    PublishingContent(
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun PublishingContent(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PulsingCircles {
                    Icon(
                        painter = painterResource(Res.drawable.ic_share_2),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(AppDimens.Size.xl6),
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.Spacing.xl4))
                Text(
                    text = stringResource(Res.string.publishing),
                    style = AppTheme.typography.title,
                    color = AppTheme.colors.onBackground,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PublishingScreenPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            PublishingContent(
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
