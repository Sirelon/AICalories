package com.sirelon.aicalories.designsystem.buttons

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MagicBlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDuration: Int = 150,
    animationEasing: Easing = EaseOut,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val radiusDp = 16.dp
    val shape = RoundedCornerShape(radiusDp)
    val mainBgColor = Color(0xB2CCFF)

    val pressed by interaction.collectIsPressedAsState()

    val transition = updateTransition(targetState = pressed, label = "BlueButtonPressedAnimation")

    val transitionSpecFunc: @Composable Transition.Segment<Boolean>.() -> FiniteAnimationSpec<Float> =
        { tween(animationDuration, easing = animationEasing) }

    val innerBorderAlpha by transition.animateFloat(
        label = "outlineBorderAlpha",
        transitionSpec = transitionSpecFunc,
        targetValueByState = { if (it) 0.0f else 1f }
    )

    val pressedBorderAlpha by transition.animateFloat(
        label = "outlineBorderAlpha",
        transitionSpec = transitionSpecFunc,
        targetValueByState = { if (it) 0.3f else 0f }
    )

    val innerShadowAlpha by transition.animateFloat(
        label = "innerShadow",
        transitionSpec = transitionSpecFunc,
        targetValueByState = { if (it) 0.25f else 0f }
    )

    val pressedShadow by transition.animateFloat(
        label = "pressedShadow",
        transitionSpec = transitionSpecFunc,
        targetValueByState = { if (it) 1f else 0f }
    )

    val outerShadow1Alpha by transition.animateFloat(
        label = "outerShadow1Alpha",
        transitionSpec = transitionSpecFunc,
        targetValueByState = { if (it) 0f else 0.25f }
    )

    val outerShadow2Alpha by transition.animateFloat(
        label = "outerShadow2Alpha",
        transitionSpec = transitionSpecFunc,
        targetValueByState = { if (it) 0f else 0.3f }
    )

    val bgColor1 by transition.animateColor(
        label = "bgColor1",
        transitionSpec = { tween(animationDuration, easing = animationEasing) },
        targetValueByState = {
            if (it) mainBgColor.copy(alpha = 0.15f) else mainBgColor.copy(alpha = 0.3f)
        }
    )

    val bgColor2 by transition.animateColor(
        label = "bgColor2",
        transitionSpec = { tween(animationDuration, easing = animationEasing) },
        targetValueByState = {
            if (it) mainBgColor.copy(alpha = 0.2f) else mainBgColor.copy(alpha = 0.1f)
        }
    )

    val translationY by transition.animateDp(
        label = "translationY",
        transitionSpec = { tween(animationDuration, easing = animationEasing) },
        targetValueByState = { if (it) 2.dp else 0.dp },
    )

    val outerPadding = 8.dp

    Box(
        modifier = modifier
            .defaultMinSize(
                minHeight = 52.dp + outerPadding,
                minWidth = 150.dp + outerPadding,
            )
            .semantics {
                role = Role.Button
            }
            .graphicsLayer {
                this.translationY = translationY.toPx()
            }
            .clickable(
                enabled = true,
                onClick = onClick,
                indication = null,
                interactionSource = interaction,
            )
    ) {
        ShadowLayer(
            outerShadow1Alpha = outerShadow1Alpha,
            outerShadow2Alpha = outerShadow2Alpha,
            pressedShadow = pressedShadow,
            radiusDp = radiusDp,
            outerPadding = outerPadding,
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(outerPadding)
                // Padding here needed to show shadows properly
                .drawWithCache {
                    val strokeWidth = 1.dp.toPx()
                    val bgGradient = Brush.verticalGradient(
                        colors = listOf(
                            bgColor1,
                            bgColor2,
                        )
                    )

                    val borderGradient = Brush.verticalGradient(
                        colors = listOf(
                            mainBgColor.copy(alpha = 0.1f),
                            mainBgColor.copy(alpha = 0.05f)
                        )
                    )

                    val cornerRadius = CornerRadius(radiusDp.toPx())
                    val borderStroke = Stroke(width = strokeWidth)

                    val innerBorderOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val innerBorderSize = size.copy(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth
                    )

                    val outerBorderSize = Size(size.width + strokeWidth, size.height + strokeWidth)
                    val outerBorderOffset = Offset(-strokeWidth / 2, -strokeWidth / 2)
                    val pressedBorderColor = Color(0xFF000000)

                    onDrawBehind {
                        // Dark Border that is visible when btn is in pressed state
                        drawRoundRect(
                            color = pressedBorderColor,
                            topLeft = outerBorderOffset,
                            size = outerBorderSize,
                            cornerRadius = cornerRadius,
                            style = borderStroke,
                            alpha = pressedBorderAlpha
                        )

                        // Button's Background
                        drawRoundRect(
                            brush = bgGradient,
                            blendMode = BlendMode.Hardlight,
                            cornerRadius = cornerRadius,
                        )

                        drawRoundRect(
                            brush = borderGradient,
                            topLeft = innerBorderOffset,
                            size = innerBorderSize,
                            cornerRadius = cornerRadius,
                            style = borderStroke,
                            alpha = innerBorderAlpha,
                        )
                    }
                }
                .innerShadow(shape = shape) {
                    this.offset = Offset(x = 0f, y = 2.dp.toPx())
                    this.radius = 1.dp.toPx()
                    this.alpha = innerShadowAlpha
                    this.color = Color(0xFF0D1626)
                }
        ) {
            ButtonText(
                modifier = Modifier.align(Alignment.Center),
                text = text,
            )
        }
    }
}

@Composable
private fun BoxScope.ShadowLayer(
    outerShadow1Alpha: Float,
    outerShadow2Alpha: Float,
    pressedShadow: Float,
    radiusDp: Dp,
    outerPadding: Dp,
) {
    val shape = RoundedCornerShape(radiusDp)
    Box(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            // Padding here needed to show shadows properly
            .padding(outerPadding)

            .dropShadow(shape = shape) {
                this.radius = 4.dp.toPx()
                this.offset = Offset(x = 0f, y = 4.dp.toPx())
                this.color = Color(0xFF000000)
                this.alpha = outerShadow1Alpha
            }
            .dropShadow(shape = shape) {
                this.radius = 4.dp.toPx()
                this.spread = -2.dp.toPx()
                this.offset = Offset(x = 0f, y = 4.dp.toPx())

                this.color = Color(0xFF000000)
                this.alpha = outerShadow2Alpha
            }
            .dropShadow(shape = shape) {
                this.color = Color(0xFF0D0F1A)
                this.alpha = pressedShadow
            }
            .drawBehind {
                val cornerRadius = CornerRadius(radiusDp.toPx())
                // clear shadow colors inside button
                drawRoundRect(
                    color = Color.Transparent,
                    cornerRadius = cornerRadius,
                    blendMode = BlendMode.Clear
                )
            },
    )
}

@Composable
private fun ButtonText(text: String, modifier: Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontWeight = FontWeight.W600,
            fontSize = 18.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    )
}

@Preview
@Composable
private fun MagicBlueButtonPreview() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFF4A5B7C))
                .blur(50.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            MagicBlueButton("Cancel", onClick = {})
        }
    }
}