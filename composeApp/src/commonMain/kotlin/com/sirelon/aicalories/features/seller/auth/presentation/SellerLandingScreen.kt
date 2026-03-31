package com.sirelon.aicalories.features.seller.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.sirelon.aicalories.composeapp.generated.resources.welcome_subtitle
import com.sirelon.aicalories.composeapp.generated.resources.welcome_to_sellsnap
import com.sirelon.aicalories.composeapp.generated.resources.why_connect_olx
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppDivider
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.IconWithBackground
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.buttons.AppButtonStyle
import com.sirelon.aicalories.designsystem.templates.TermsAndPrivacy
import com.sirelon.aicalories.designsystem.templates.TitleWithSubtitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SellerLandingScreen(
    onContinueWithOlx: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.Spacing.xl6)
                .padding(top = AppDimens.Spacing.xl10),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl10)
        ) {
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

            TitleWithSubtitle(
                title = stringResource(Res.string.welcome_to_sellsnap),
                subtitle = stringResource(Res.string.welcome_subtitle),
            )

            ContinueWithOlxBlock(onContinueWithOlx = onContinueWithOlx)

            // Divider with "or"
            AppDivider(
                middleContent = {
                    Text(
                        text = stringResource(Res.string.or_divider),
                        modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.onSurfaceSoft
                    )
                },
            )

            ContinueAsGuestBlock(onContinueAsGuest = onContinueAsGuest)

            TermsAndPrivacy(onTermsClick = onTermsClick, onPrivacyClick = onPrivacyClick)
        }
    }
}

@Composable
private fun ContinueAsGuestBlock(onContinueAsGuest: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)) {
        // Continue as Guest Info
        ContinueAsGuestInfoBlock()
        AppButton(
            text = stringResource(Res.string.continue_as_guest),
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth(),
            style = AppButtonDefaults.outline(),
            leadingIcon = Icons.Default.Person
        )
    }
}

@Composable
private fun ContinueWithOlxBlock(onContinueWithOlx: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)) {
        // Why connect to OLX card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))// Light yellowish background
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
    }
}

@Composable
private fun ContinueAsGuestInfoBlock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.BorderRadius.xl)),
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
        SellerLandingScreen(
            onContinueWithOlx = { },
            onContinueAsGuest = { },
            onTermsClick = { },
            onPrivacyClick = { },
        )
    }
}
