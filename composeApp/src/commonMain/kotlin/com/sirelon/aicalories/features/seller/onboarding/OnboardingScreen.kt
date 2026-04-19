package com.sirelon.aicalories.features.seller.onboarding

import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.PulsingCircles
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.buttons.AppIconButton
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.get_started
import com.sirelon.aicalories.generated.resources.ic_arrow_left
import com.sirelon.aicalories.generated.resources.ic_arrow_right
import com.sirelon.aicalories.generated.resources.ic_camera
import com.sirelon.aicalories.generated.resources.ic_check
import com.sirelon.aicalories.generated.resources.ic_sparkles
import com.sirelon.aicalories.generated.resources.next
import com.sirelon.aicalories.generated.resources.onboarding_step1_subtitle
import com.sirelon.aicalories.generated.resources.onboarding_step1_title
import com.sirelon.aicalories.generated.resources.onboarding_step2_subtitle
import com.sirelon.aicalories.generated.resources.onboarding_step2_title
import com.sirelon.aicalories.generated.resources.onboarding_step3_pill
import com.sirelon.aicalories.generated.resources.onboarding_step3_subtitle
import com.sirelon.aicalories.generated.resources.onboarding_step3_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private sealed interface OnboardingStepIcon {
    data object Snap : OnboardingStepIcon
    data object AiWrites : OnboardingStepIcon
    data object Done : OnboardingStepIcon
}

private data class OnboardingItem(
    val title: String,
    val subtitle: String,
    val icon: OnboardingStepIcon,
)

@Composable
private fun onboardingItems() = listOf(
    OnboardingItem(
        title = stringResource(Res.string.onboarding_step1_title),
        subtitle = stringResource(Res.string.onboarding_step1_subtitle),
        icon = OnboardingStepIcon.Snap,
    ),
    OnboardingItem(
        title = stringResource(Res.string.onboarding_step2_title),
        subtitle = stringResource(Res.string.onboarding_step2_subtitle),
        icon = OnboardingStepIcon.AiWrites,
    ),
    OnboardingItem(
        title = stringResource(Res.string.onboarding_step3_title),
        subtitle = stringResource(Res.string.onboarding_step3_subtitle),
        icon = OnboardingStepIcon.Done,
    ),
)

@Composable
fun OnboardingScreen(onClose: () -> Unit) {
    val items = onboardingItems()
    val state = rememberPagerState { items.size }
    AppScaffold(
        bottomBar = {
            BottomButtons(state = state, onClose = onClose)
        }
    ) {
        HorizontalPager(state = state) {
            OnboardingPage(items[it])
        }
    }
}

@Composable
private fun OnboardingPage(item: OnboardingItem) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppDimens.Spacing.xl3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            AppDimens.Spacing.xl3,
            Alignment.CenterVertically,
        ),
    ) {
        FadeInUp(durationMs = 380) {
            OnboardingStepIconContent(item.icon)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
        ) {
            FadeInUp(durationMs = 460) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = item.title,
                    style = AppTheme.typography.headline,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
            FadeInUp(durationMs = 540) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = item.subtitle,
                    style = AppTheme.typography.title,
                    color = AppTheme.colors.onSurfaceSoft,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FadeInUp(
    durationMs: Int,
    translation: Dp = AppDimens.Spacing.xl3,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs,
                easing = FastOutSlowInEasing,
            ),
        )
    }
    val translationPx = with(LocalDensity.current) { translation.toPx() }
    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * translationPx
            },
    ) {
        content()
    }
}

@Composable
private fun OnboardingStepIconContent(icon: OnboardingStepIcon) {
    when (icon) {
        OnboardingStepIcon.Snap -> SnapStepIcon()
        OnboardingStepIcon.AiWrites -> AiWritesStepIcon()
        OnboardingStepIcon.Done -> DoneStepIcon()
    }
}

// warningVariant stays a fixed bright yellow in both themes, so the sparkle
// tint must stay dark for legibility regardless of the active theme.
private val SparkleBadgeTint = Color(0xFF3A1F00)

@Composable
private fun SnapStepIcon() {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppDimens.BorderRadius.xl11)
    Box(
        modifier = Modifier
            .size(AppDimens.Size.xl22)
            .padding(AppDimens.Spacing.xl3)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = AppDimens.Spacing.xl5,
                    shape = shape,
                    ambientColor = colors.primary,
                    spotColor = colors.primary,
                )
                .background(
                    brush = Brush.linearGradient(
                        listOf(colors.primaryBright, colors.primary),
                    ),
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_camera),
                contentDescription = null,
                modifier = Modifier.size(AppDimens.Size.xl14),
                tint = colors.onPrimary,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = AppDimens.Spacing.m, y = -AppDimens.Spacing.m)
                .size(AppDimens.Size.xl11)
                .shadow(
                    elevation = AppDimens.Spacing.m,
                    shape = CircleShape,
                    ambientColor = colors.warning,
                    spotColor = colors.warning,
                )
                .background(colors.warningVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_sparkles),
                contentDescription = null,
                modifier = Modifier.size(AppDimens.Size.xl6),
                tint = SparkleBadgeTint,
            )
        }
    }
}

@Composable
private fun AiWritesStepIcon() {
    Box(
        modifier = Modifier.size(AppDimens.Size.xl22),
        contentAlignment = Alignment.Center,
    ) {
        PulsingCircles {
            Icon(
                painter = painterResource(Res.drawable.ic_sparkles),
                contentDescription = null,
                modifier = Modifier.size(AppDimens.Size.xl8),
                tint = AppTheme.colors.onPrimary,
            )
        }
    }
}

@Composable
private fun DoneStepIcon() {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppDimens.BorderRadius.xl11)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        modifier = Modifier
            .shadow(
                elevation = AppDimens.Spacing.xl5,
                shape = shape,
                ambientColor = colors.onSurface,
                spotColor = colors.onSurface,
            )
            .background(
                brush = Brush.linearGradient(
                    listOf(colors.surfaceLowest, colors.surfaceLow),
                ),
                shape = shape,
            )
            .border(
                width = AppDimens.BorderWidth.s,
                color = colors.outlineVariant.copy(alpha = 0.33f),
                shape = shape,
            )
            .padding(AppDimens.Spacing.xl8),
    ) {
        Box(
            modifier = Modifier
                .background(colors.success, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(AppDimens.Size.xl12),
                tint = colors.onPrimary,
            )
        }

        Box(
            modifier = Modifier
                .padding(bottom = AppDimens.Spacing.xl2)
                .background(
                    color = colors.success.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(AppDimens.BorderRadius.m),
                )
                .padding(
                    horizontal = AppDimens.Spacing.l,
                    vertical = AppDimens.Spacing.xs,
                ),
        ) {
            Text(
                text = stringResource(Res.string.onboarding_step3_pill),
                style = AppTheme.typography.caption.copy(
                    color = colors.success,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
            )
        }
    }
}

@Composable
private fun BottomButtons(
    state: PagerState,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    LookaheadScope {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.Spacing.xl)
                .animateBounds(this)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            if (state.currentPage > 0) {
                AppIconButton(
                    icon = painterResource(Res.drawable.ic_arrow_left),
                    onClick = {
                        scope.launch {
                            state.animateScrollToPage(state.currentPage - 1)
                        }
                    },
                )
            }

            val lastPage = state.currentPage == state.pageCount - 1
            val text = if (lastPage) {
                stringResource(Res.string.get_started)
            } else {
                stringResource(Res.string.next)
            }

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                style = AppButtonDefaults.secondary(),
                onClick = {
                    if (lastPage) {
                        onClose()
                    } else {
                        scope.launch {
                            state.animateScrollToPage(state.currentPage + 1)
                        }
                    }
                },
                trailingIcon = painterResource(Res.drawable.ic_arrow_right),
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen {}
    }
}
