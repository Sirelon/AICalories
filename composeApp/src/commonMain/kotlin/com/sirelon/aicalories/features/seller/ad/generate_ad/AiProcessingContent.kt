package com.sirelon.aicalories.features.seller.ad.generate_ad

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.PulsingCircles
import com.sirelon.aicalories.designsystem.templates.TitleWithSubtitle
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ai_analyzing_photo
import com.sirelon.aicalories.generated.resources.ai_creating_ad_title
import com.sirelon.aicalories.generated.resources.ai_step_uploading_photos
import com.sirelon.aicalories.generated.resources.ai_step_analyzing_image
import com.sirelon.aicalories.generated.resources.ai_step_calculating_price
import com.sirelon.aicalories.generated.resources.ai_step_generating_title
import com.sirelon.aicalories.generated.resources.ai_step_writing_description
import com.sirelon.aicalories.generated.resources.ic_check
import com.sirelon.aicalories.generated.resources.ic_sparkles
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
private fun processingSteps() = listOf(
    stringResource(Res.string.ai_step_uploading_photos),
    stringResource(Res.string.ai_step_analyzing_image),
    stringResource(Res.string.ai_step_generating_title),
    stringResource(Res.string.ai_step_writing_description),
    stringResource(Res.string.ai_step_calculating_price),
)

@Composable
fun AiProcessingContent(
    completedSteps: Int,
    modifier: Modifier = Modifier,
) {
    val steps = processingSteps()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppDimens.Spacing.xl6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Pulsing circles with spinning icon + bouncing badge
        Box(contentAlignment = Alignment.Center) {
            PulsingCircles {
                SpinningIcon()
            }
            BouncingBadge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (-4).dp),
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.Spacing.xl8))

        TitleWithSubtitle(
            title = stringResource(Res.string.ai_creating_ad_title),
            subtitle = stringResource(Res.string.ai_analyzing_photo),
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing.xl6))

        ProcessingStepsList(steps = steps, completedSteps = completedSteps)
    }
}

@Composable
private fun SpinningIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000),
        ),
    )

    Icon(
        painter = painterResource(Res.drawable.ic_sparkles),
        contentDescription = null,
        modifier = modifier
            .size(AppDimens.Size.xl8)
            .graphicsLayer { rotationZ = rotation },
        tint = AppTheme.colors.onPrimary,
    )
}

@Composable
private fun BouncingBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.translationY = offsetY
            }
            .size(AppDimens.Size.xl8)
            .background(AppTheme.colors.success, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.Size.xl3),
            tint = AppTheme.colors.onPrimary,
        )
    }
}

@Composable
private fun ProcessingStepsList(
    steps: List<String>,
    completedSteps: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(AppDimens.Size.xl24),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
    ) {
        steps.forEachIndexed { index, stepText ->
            val isDone = index < completedSteps
            ProcessingStepItem(text = stepText, isDone = isDone)
        }
    }
}

@Composable
private fun ProcessingStepItem(
    text: String,
    isDone: Boolean,
    modifier: Modifier = Modifier,
) {
    val successColor = AppTheme.colors.success
    val idleColor = AppTheme.colors.onSurfaceSoft
    val textDoneColor = AppTheme.colors.onSurface
    val textIdleColor = AppTheme.colors.onSurfaceSoft

    val backgroundColor by animateColorAsState(
        targetValue = if (isDone) successColor else idleColor,
        animationSpec = tween(durationMillis = 300),
        label = "bg"
    )

    val textColor = if (isDone) textDoneColor else textIdleColor

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
    ) {
        Box(
            modifier = Modifier
                .size(AppDimens.Size.xl6)
                .graphicsLayer {
                    clip = true
                    shape = CircleShape
                }
                .drawWithCache {
                    val radius = size.minDimension / 2f
                    onDrawBehind {
                        drawCircle(
                            color = backgroundColor,
                            radius = radius
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isDone) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.Size.xl2),
                    tint = AppTheme.colors.onPrimary,
                )
            } else {
                PingingDot()
            }
        }

        Text(
            text = text,
            fontSize = AppDimens.TextSize.xl2,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}

@Composable
private fun PingingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        modifier = modifier
            .size(AppDimens.Size.m)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(AppTheme.colors.onSurfaceMuted, CircleShape),
    )
}
