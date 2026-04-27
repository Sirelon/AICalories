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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.PulsingCircles
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ic_share_2
import com.sirelon.aicalories.generated.resources.publishing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PublishingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
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

@PreviewLightDark
@Composable
private fun PublishingOverlayPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            PublishingOverlay()
        }
    }
}
