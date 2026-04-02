package com.sirelon.aicalories.features.seller.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.*
import com.sirelon.aicalories.designsystem.AppCard
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.buttons.AppButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun SellerAuthScreen(
    state: SellerAuthContract.SellerAuthState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SellerAuthContract.SellerAuthEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.Spacing.xl4),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = stringResource(Res.string.olx_auth_screen_title),
                fontSize = AppDimens.TextSize.xl7,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.onBackground,
            )
            Text(
                text = stringResource(Res.string.olx_auth_screen_subtitle),
                fontSize = AppDimens.TextSize.xl3,
                color = AppTheme.colors.onSurfaceSoft,
            )

            AppCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(AppDimens.Spacing.xl4),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l),
                ) {
                    Text(
                        text = if (state.isAuthorized) stringResource(Res.string.olx_auth_connected) else state.statusLabel,
                        fontSize = AppDimens.TextSize.xl4,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.statusMessage,
                        color = AppTheme.colors.onSurfaceSoft,
                    )
                    state.accessTokenExpiresAtEpochSeconds?.let { expiresAt ->
                        Text(
                            text = stringResource(Res.string.token_expires_at, expiresAt),
                            color = AppTheme.colors.onSurfaceSoft,
                        )
                    }
                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = AppTheme.colors.error,
                        )
                    }
                }
            }

            AppCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(AppDimens.Spacing.xl4),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l),
                ) {
                    AppButton(
                        text = stringResource(Res.string.olx_auth_connect),
                        onClick = { onEvent(SellerAuthContract.SellerAuthEvent.ConnectClicked) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l),
                    ) {
                        AppButton(
                            text = stringResource(Res.string.olx_auth_refresh_token),
                            onClick = { onEvent(SellerAuthContract.SellerAuthEvent.RefreshClicked) },
                            modifier = Modifier.weight(1f),
                        )
                        AppButton(
                            text = stringResource(Res.string.olx_auth_test_me),
                            onClick = { onEvent(SellerAuthContract.SellerAuthEvent.TestMeClicked) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    AppButton(
                        text = stringResource(Res.string.olx_auth_disconnect),
                        onClick = { onEvent(SellerAuthContract.SellerAuthEvent.DisconnectClicked) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            state.me?.let { me ->
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(AppDimens.Spacing.xl4),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                    ) {
                        Text(
                            text = stringResource(Res.string.olx_users_me),
                            fontSize = AppDimens.TextSize.xl4,
                            fontWeight = FontWeight.Bold,
                        )
                        HorizontalDivider()
                        ProfileRow(label = stringResource(Res.string.profile_field_id), value = me.id.toString())
                        ProfileRow(label = stringResource(Res.string.profile_field_name), value = me.name)
                        ProfileRow(label = stringResource(Res.string.profile_field_email), value = me.email)
                        ProfileRow(label = stringResource(Res.string.profile_field_status), value = me.status)
                        ProfileRow(label = stringResource(Res.string.profile_field_phone), value = me.phone)
                        ProfileRow(label = stringResource(Res.string.profile_field_business), value = me.isBusiness?.toString())
                    }
                }
            }

            if (state.isBusy) {
                Spacer(modifier = Modifier.height(AppDimens.Spacing.xl4))
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.onSurfaceSoft,
        )
        Text(text = value, color = AppTheme.colors.onSurface)
    }
}
