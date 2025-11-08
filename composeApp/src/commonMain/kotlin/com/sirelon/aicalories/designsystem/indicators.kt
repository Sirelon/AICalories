package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

private const val COMPLETE_PERCENTAGE = 100.0

@Composable
fun BoxScope.UploadStatusIndicator(
    progress: Double,
) {
    if (progress >= COMPLETE_PERCENTAGE) return

    // TODO: use colors from AppTheme
    val percent = progress.coerceIn(0.0, COMPLETE_PERCENTAGE)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .align(Alignment.BottomCenter),
        color = Color.Black.copy(alpha = 0.4f),
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.Spacing.xl,
                    vertical = AppDimens.Spacing.m,
                ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
        ) {
            LinearProgressIndicator(
                progress = { (percent / COMPLETE_PERCENTAGE).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
            Text(
                text = "${percent.toInt()}%",
                style = AppTheme.typography.caption,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}