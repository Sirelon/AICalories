package com.sirelon.aicalories.features.seller.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.benefit_manage
import com.sirelon.aicalories.composeapp.generated.resources.benefit_publish
import com.sirelon.aicalories.composeapp.generated.resources.benefit_sync
import com.sirelon.aicalories.composeapp.generated.resources.continue_as_guest
import com.sirelon.aicalories.composeapp.generated.resources.continue_with_olx
import com.sirelon.aicalories.composeapp.generated.resources.guest_description
import com.sirelon.aicalories.composeapp.generated.resources.ic_snap_logo
import com.sirelon.aicalories.composeapp.generated.resources.or_divider
import com.sirelon.aicalories.composeapp.generated.resources.privacy_policy
import com.sirelon.aicalories.composeapp.generated.resources.step_format
import com.sirelon.aicalories.composeapp.generated.resources.terms_of_service
import com.sirelon.aicalories.composeapp.generated.resources.terms_privacy_prefix
import com.sirelon.aicalories.composeapp.generated.resources.welcome_subtitle
import com.sirelon.aicalories.composeapp.generated.resources.welcome_to_sellsnap
import com.sirelon.aicalories.composeapp.generated.resources.why_connect_olx
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.IconWithBackground
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.buttons.AppButtonStyle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SellerLandingScreen(
    onBackClick: () -> Unit = {},
    onContinueWithOlx: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LandingTopBar(
                step = 1,
                totalSteps = 2,
                onBackClick = onBackClick
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.Spacing.xl6),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl10))

            // Logo Section
            IconWithBackground(
                backgroundColor = AppTheme.colors.warning,
                modifier = Modifier.size(80.dp),
                iconPadding = 20.dp
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_snap_logo),
                    contentDescription = null,
                    tint = AppTheme.colors.onPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl6))

            Text(
                text = stringResource(Res.string.welcome_to_sellsnap),
                style = AppTheme.typography.headline,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onBackground
            )

            Text(
                text = stringResource(Res.string.welcome_subtitle),
                style = AppTheme.typography.title,
                color = AppTheme.colors.onSurfaceSoft,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl10))

            // Why connect to OLX card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.BorderRadius.xl),
                color = Color(0xFFFFFBEB) // Light yellowish background
            ) {
                Column(
                    modifier = Modifier.padding(AppDimens.Spacing.xl6),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
                ) {
                    Text(
                        text = stringResource(Res.string.why_connect_olx),
                        style = AppTheme.typography.title,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onBackground
                    )

                    BenefitItem(text = stringResource(Res.string.benefit_publish))
                    BenefitItem(text = stringResource(Res.string.benefit_sync))
                    BenefitItem(text = stringResource(Res.string.benefit_manage))
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl10))

            // Divider with "or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.outline.copy(alpha = 0.5f))
                Text(
                    text = stringResource(Res.string.or_divider),
                    modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.onSurfaceSoft
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.outline.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl10))

            // Continue as Guest Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.BorderRadius.xl))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onContinueAsGuest
                    ),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(AppDimens.BorderRadius.m))
                        .background(AppTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AppTheme.colors.onSurfaceSoft
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Res.string.continue_as_guest),
                        style = AppTheme.typography.title,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onBackground
                    )
                    Text(
                        text = stringResource(Res.string.guest_description),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.onSurfaceSoft,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl10))

            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4)
            ) {
                AppButton(
                    text = stringResource(Res.string.continue_with_olx),
                    onClick = onContinueWithOlx,
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyle(
                        backgroundColor = Color(0xFF5AB4C6), // OLX Teal
                        contentColor = Color.White
                    ),
                    leadingIcon = null
                )

                AppButton(
                    text = stringResource(Res.string.continue_as_guest),
                    onClick = onContinueAsGuest,
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonDefaults.outline(),
                    leadingIcon = Icons.Default.Person
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl6))

            // Terms and Privacy
            val termsOfService = stringResource(Res.string.terms_of_service)
            val privacyPolicy = stringResource(Res.string.privacy_policy)
            val annotatedString = buildAnnotatedString {
                append(stringResource(Res.string.terms_privacy_prefix))
                pushStringAnnotation(tag = "terms", annotation = "terms")
                withStyle(style = SpanStyle(color = AppTheme.colors.warning, textDecoration = TextDecoration.Underline)) {
                    append(termsOfService)
                }
                pop()
                append(" " + stringResource(Res.string.or_divider).replace("or", "and") + " ")
                pushStringAnnotation(tag = "privacy", annotation = "privacy")
                withStyle(style = SpanStyle(color = AppTheme.colors.warning, textDecoration = TextDecoration.Underline)) {
                    append(privacyPolicy)
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                style = AppTheme.typography.caption.copy(
                    color = AppTheme.colors.onSurfaceSoft,
                    textAlign = TextAlign.Center
                ),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                        .firstOrNull()?.let { onTermsClick() }
                    annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                        .firstOrNull()?.let { onPrivacyClick() }
                }
            )
        }
    }
}

@Composable
private fun LandingTopBar(
    step: Int,
    totalSteps: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.Spacing.xl4, vertical = AppDimens.Spacing.xl3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = AppTheme.colors.onBackground
            )
        }

        Text(
            text = stringResource(Res.string.step_format, step.toString(), totalSteps.toString()),
            style = AppTheme.typography.label,
            color = AppTheme.colors.onSurfaceSoft
        )
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF1B8E5A),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = AppTheme.typography.body,
            color = AppTheme.colors.onBackground
        )
    }
}

@Preview
@Composable
private fun SellerLandingScreenPreview() {
    AppTheme {
        SellerLandingScreen()
    }
}
