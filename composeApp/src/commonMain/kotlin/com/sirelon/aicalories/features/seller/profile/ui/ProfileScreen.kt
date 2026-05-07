package com.sirelon.aicalories.features.seller.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.permissions.CoarseLocation
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppAsyncImage
import com.sirelon.aicalories.designsystem.AppCard
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.Cell
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.buttons.AppButtonStyle
import com.sirelon.aicalories.designsystem.screens.LoadingOverlay
import com.sirelon.aicalories.features.media.PermissionDialogContent
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthCallbackBridge
import com.sirelon.aicalories.features.seller.auth.domain.OlxUser
import com.sirelon.aicalories.features.seller.location.OlxLocation
import com.sirelon.aicalories.features.seller.profile.presentation.ProfileContract
import com.sirelon.aicalories.features.seller.profile.presentation.ProfileContract.ProfileEvent
import com.sirelon.aicalories.features.seller.profile.presentation.ProfileViewModel
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.back
import com.sirelon.aicalories.generated.resources.change_button
import com.sirelon.aicalories.generated.resources.continue_with_olx
import com.sirelon.aicalories.generated.resources.ic_arrow_left
import com.sirelon.aicalories.generated.resources.ic_refresh_cw
import com.sirelon.aicalories.generated.resources.ic_user
import com.sirelon.aicalories.generated.resources.location_detecting
import com.sirelon.aicalories.generated.resources.location_not_available
import com.sirelon.aicalories.generated.resources.location_rationale_message
import com.sirelon.aicalories.generated.resources.location_rationale_title
import com.sirelon.aicalories.generated.resources.location_settings_message_android
import com.sirelon.aicalories.generated.resources.location_settings_message_ios
import com.sirelon.aicalories.generated.resources.location_settings_title
import com.sirelon.aicalories.generated.resources.not_now
import com.sirelon.aicalories.generated.resources.open_settings
import com.sirelon.aicalories.generated.resources.profile_field_business
import com.sirelon.aicalories.generated.resources.profile_field_created_at
import com.sirelon.aicalories.generated.resources.profile_field_email
import com.sirelon.aicalories.generated.resources.profile_field_id
import com.sirelon.aicalories.generated.resources.profile_field_last_login_at
import com.sirelon.aicalories.generated.resources.profile_field_name
import com.sirelon.aicalories.generated.resources.profile_field_phone
import com.sirelon.aicalories.generated.resources.profile_field_status
import com.sirelon.aicalories.generated.resources.profile_guest_description
import com.sirelon.aicalories.generated.resources.profile_guest_title
import com.sirelon.aicalories.generated.resources.profile_location_subtitle
import com.sirelon.aicalories.generated.resources.profile_location_title
import com.sirelon.aicalories.generated.resources.profile_logout
import com.sirelon.aicalories.generated.resources.profile_not_provided
import com.sirelon.aicalories.generated.resources.profile_olx_account
import com.sirelon.aicalories.generated.resources.profile_screen_title
import com.sirelon.aicalories.generated.resources.profile_value_no
import com.sirelon.aicalories.generated.resources.profile_value_yes
import com.sirelon.aicalories.generated.resources.retry
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreenRoute(
    onBack: () -> Unit,
    onOpenOlxAuth: (String) -> Unit,
    onLogout: () -> Unit,
    reason: String? = null,
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val locationPermissionController = rememberPermissionController(permission = Permission.CoarseLocation)

    LaunchedEffect(viewModel) {
        OlxAuthCallbackBridge.callbacks.collect { callbackUrl ->
            viewModel.onCallbackReceived(callbackUrl)
        }
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is ProfileContract.ProfileEffect.LaunchOlxAuthFlow -> {
                onOpenOlxAuth(effect.url)
            }

            is ProfileContract.ProfileEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }

            ProfileContract.ProfileEffect.NavigateToLanding -> onLogout()
        }
    }

    LoadingOverlay(
        isLoading = state.isLoading || state.isAuthenticating,
    ) {
        ProfileScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onEvent = viewModel::onEvent,
            onChangeLocation = {
                locationPermissionController.requestPermission {
                    viewModel.onEvent(ProfileEvent.ChangeLocationClicked)
                }
            },
            reason = reason,
        )
    }

    PermissionDialogs(
        controller = locationPermissionController,
        rationaleContent = PermissionDialogContent(
            title = Res.string.location_rationale_title,
            message = Res.string.location_rationale_message,
            confirmText = Res.string.retry,
            dismissText = Res.string.not_now,
        ),
        settingsContentProvider = { isIos ->
            PermissionDialogContent(
                title = Res.string.location_settings_title,
                message = if (isIos) {
                    Res.string.location_settings_message_ios
                } else {
                    Res.string.location_settings_message_android
                },
                confirmText = Res.string.open_settings,
                dismissText = Res.string.not_now,
            )
        },
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileContract.ProfileState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEvent: (ProfileEvent) -> Unit,
    onChangeLocation: () -> Unit,
    reason: String? = null,
) {
    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.profile_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_left),
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(ProfileEvent.RefreshClicked) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh_cw),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
        ) {
            if (!reason.isNullOrBlank()) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = reason,
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.error,
                        modifier = Modifier.padding(AppDimens.Spacing.xl5),
                    )
                }
            }

            if (state.user == null) {
                GuestCard(onLogin = { onEvent(ProfileEvent.LoginClicked) })
            } else {
                AccountCard(
                    user = state.user,
                    onLogout = { onEvent(ProfileEvent.LogoutClicked) },
                )
            }

            LocationCard(
                location = state.location,
                isLoading = state.isLocationLoading,
                onChangeLocation = onChangeLocation,
            )

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.error,
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl2))
        }
    }
}

@Composable
private fun GuestCard(onLogin: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
            horizontalAlignment = Alignment.Start,
        ) {
            ProfileAvatar(avatarUrl = null, fallbackInitial = null)
            Text(
                text = stringResource(Res.string.profile_guest_title),
                style = AppTheme.typography.title,
                color = AppTheme.colors.onSurface,
            )
            Text(
                text = stringResource(Res.string.profile_guest_description),
                style = AppTheme.typography.body,
                color = AppTheme.colors.onSurfaceMuted,
            )
            AppButton(
                text = stringResource(Res.string.continue_with_olx),
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                style = AppButtonDefaults.primary(),
            )
        }
    }
}

@Composable
private fun AccountCard(
    user: OlxUser,
    onLogout: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(vertical = AppDimens.Spacing.xl2),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.Spacing.xl5),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
            ) {
                ProfileAvatar(
                    avatarUrl = user.avatar,
                    fallbackInitial = user.name.firstOrNull()?.uppercaseChar()?.toString(),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.takeIf { it.isNotBlank() }
                            ?: stringResource(Res.string.profile_olx_account),
                        style = AppTheme.typography.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = user.email.takeIf { it.isNotBlank() }
                            ?: stringResource(Res.string.profile_not_provided),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            ProfileField(label = stringResource(Res.string.profile_field_id), value = user.id.toString())
            ProfileField(label = stringResource(Res.string.profile_field_name), value = user.name)
            ProfileField(label = stringResource(Res.string.profile_field_email), value = user.email)
            ProfileField(label = stringResource(Res.string.profile_field_status), value = user.status)
            ProfileField(label = stringResource(Res.string.profile_field_phone), value = user.phone)
            ProfileField(label = stringResource(Res.string.profile_field_created_at), value = user.createdAt)
            ProfileField(label = stringResource(Res.string.profile_field_last_login_at), value = user.lastLoginAt)
            ProfileField(
                label = stringResource(Res.string.profile_field_business),
                value = if (user.isBusiness) {
                    stringResource(Res.string.profile_value_yes)
                } else {
                    stringResource(Res.string.profile_value_no)
                },
            )

            AppButton(
                text = stringResource(Res.string.profile_logout),
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.Spacing.xl5),
                style = AppButtonStyle(
                    backgroundColor = AppTheme.colors.error,
                    contentColor = AppTheme.colors.onError,
                ),
            )
        }
    }
}

@Composable
private fun LocationCard(
    location: OlxLocation?,
    isLoading: Boolean,
    onChangeLocation: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl5),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl4),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimens.Size.xl11)
                        .clip(CircleShape)
                        .background(AppTheme.colors.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppTheme.colors.onSecondaryContainer,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.profile_location_title),
                        style = AppTheme.typography.title,
                    )
                    Text(
                        text = stringResource(Res.string.profile_location_subtitle),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.onSurfaceMuted,
                    )
                }
            }

            Text(
                text = when {
                    isLoading -> stringResource(Res.string.location_detecting)
                    location != null -> location.displayName
                    else -> stringResource(Res.string.location_not_available)
                },
                style = AppTheme.typography.body,
                color = if (location == null) AppTheme.colors.onSurfaceMuted else AppTheme.colors.onSurface,
            )

            AppButton(
                text = stringResource(Res.string.change_button),
                onClick = onChangeLocation,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                style = AppButtonDefaults.outline(),
            )
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String?,
) {
    Cell(
        headline = {
            Text(
                text = value?.takeIf { it.isNotBlank() }
                    ?: stringResource(Res.string.profile_not_provided),
                style = AppTheme.typography.body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        overline = {
            Text(
                text = label,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurfaceMuted,
            )
        },
    )
}

@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    fallbackInitial: String?,
) {
    Box(
        modifier = Modifier
            .size(AppDimens.Size.xl14)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(AppTheme.colors.primaryBright, AppTheme.colors.primary),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AppAsyncImage(
                model = avatarUrl,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (!fallbackInitial.isNullOrBlank()) {
            Text(
                text = fallbackInitial,
                style = AppTheme.typography.title,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onPrimary,
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_user),
                contentDescription = null,
                tint = AppTheme.colors.onPrimary,
            )
        }
    }
}
