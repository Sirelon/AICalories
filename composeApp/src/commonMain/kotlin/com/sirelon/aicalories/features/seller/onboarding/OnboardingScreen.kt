package com.sirelon.aicalories.features.seller.onboarding

import androidx.compose.animation.animateBounds
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.IconWithBackground
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.buttons.AppIconButton
import com.sirelon.aicalories.designsystem.templates.TitleWithSubtitle
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.compose_multiplatform
import com.sirelon.aicalories.generated.resources.get_started
import com.sirelon.aicalories.generated.resources.ic_arrow_left
import com.sirelon.aicalories.generated.resources.ic_arrow_right
import com.sirelon.aicalories.generated.resources.ic_snap_logo
import com.sirelon.aicalories.generated.resources.next
import com.sirelon.aicalories.generated.resources.onboarding_step1_subtitle
import com.sirelon.aicalories.generated.resources.onboarding_step1_title
import com.sirelon.aicalories.generated.resources.onboarding_step2_subtitle
import com.sirelon.aicalories.generated.resources.onboarding_step2_title
import com.sirelon.aicalories.generated.resources.onboarding_step3_subtitle
import com.sirelon.aicalories.generated.resources.onboarding_step3_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class OnboardingItem(
    val title: String,
    val subtitle: String,
    val image: DrawableResource,
    val icon: DrawableResource,
)

// TODO: replace images and icons
@Composable
private fun onboardingItems() = listOf(
    OnboardingItem(
        title = stringResource(Res.string.onboarding_step1_title),
        subtitle = stringResource(Res.string.onboarding_step1_subtitle),
        image = Res.drawable.compose_multiplatform,
        icon = Res.drawable.ic_snap_logo
    ),
    OnboardingItem(
        title = stringResource(Res.string.onboarding_step2_title),
        subtitle = stringResource(Res.string.onboarding_step2_subtitle),
        image = Res.drawable.compose_multiplatform,
        icon = Res.drawable.ic_snap_logo
    ),
    OnboardingItem(
        title = stringResource(Res.string.onboarding_step3_title),
        subtitle = stringResource(Res.string.onboarding_step3_subtitle),
        image = Res.drawable.compose_multiplatform,
        icon = Res.drawable.ic_snap_logo
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
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = AppDimens.Spacing.xl9)
        ) {
            Image(
                modifier = Modifier.clip(RoundedCornerShape(AppDimens.BorderRadius.l)),
                painter = painterResource(item.image), contentDescription = null
            )

            val iconSize = AppDimens.Size.xl11
            IconWithBackground(
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.BottomEnd),
                backgroundColor = AppTheme.colors.infoSurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = AppTheme.colors.primary
                )
            }
        }

        TitleWithSubtitle(
            title = item.title,
            subtitle = item.subtitle,
        )
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

