package com.sirelon.aicalories.features.seller.ad.generate_ad

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.describe_item_placeholder
import com.sirelon.aicalories.generated.resources.describe_your_item
import com.sirelon.aicalories.generated.resources.generate_ad_with_ai
import com.sirelon.aicalories.generated.resources.generating
import com.sirelon.aicalories.generated.resources.ic_snap_logo
import com.sirelon.aicalories.generated.resources.sellsnap_title
import com.sirelon.aicalories.generated.resources.snap_photo_ad_desc
import com.sirelon.aicalories.generated.resources.tip_angles
import com.sirelon.aicalories.generated.resources.tip_defects
import com.sirelon.aicalories.generated.resources.tip_lighting
import com.sirelon.aicalories.generated.resources.tips_for_better_photos
import com.sirelon.aicalories.generated.resources.turn_stuff_into_olx_listings
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.IconWithBackground
import com.sirelon.aicalories.designsystem.Input
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.features.media.ui.PhotosSection
import com.sirelon.aicalories.features.seller.ad.Advertisement
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GenerateAdScreen(
    onBack: () -> Unit,
    openAdPreview: (Advertisement) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GenerateAdViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformContext = LocalPlatformContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionController = rememberPermissionController(permission = Permission.Camera)

    val photoPicker = rememberPhotoPickerController(
        permissionController = permissionController,
        onResult = { selectionResult ->
            viewModel.onEvent(
                GenerateAdContract.GenerateAdEvent.UploadFilesResult(
                    platformContext = platformContext,
                    result = selectionResult,
                )
            )
        },
    )

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is GenerateAdContract.GenerateAdEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }

            is GenerateAdContract.GenerateAdEffect.OpenAdPreview -> openAdPreview(effect.ad)
        }
    }

    AnimatedContent(state.isLoading) {
        if (it) {
            AiProcessingContent()
        } else {
            GenerateAdScreenContent(
                state = state,
                snackbarHostState = snackbarHostState,
                onPromptChanged = {
                    viewModel.onEvent(GenerateAdContract.GenerateAdEvent.PromptChanged(it))
                },
                onTakePhotoClick = {
                    if (!state.isLoading) {
                        photoPicker.captureWithCamera()
                    }
                },
                onUploadClick = {
                    if (!state.isLoading) {
                        photoPicker.pickFromGallery()
                    }
                },
                onSubmitClick = {
                    if (state.canSubmit) {
                        viewModel.onEvent(GenerateAdContract.GenerateAdEvent.Submit(platformContext))
                    }
                },
                modifier = modifier,
            )
        }
    }

    PermissionDialogs(
        controller = permissionController,
    )
}

@Composable
private fun GenerateAdScreenContent(
    state: GenerateAdContract.GenerateAdState,
    snackbarHostState: SnackbarHostState,
    onPromptChanged: (String) -> Unit,
    onTakePhotoClick: () -> Unit,
    onUploadClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AppDimens.Spacing.xl3),
                style = AppButtonDefaults.primary(),
                text = if (state.isLoading) stringResource(Res.string.generating) else stringResource(Res.string.generate_ad_with_ai),
                onClick = onSubmitClick,
                leadingIcon = if (state.isLoading) null else painterResource(Res.drawable.ic_sparkles),
                enabled = state.canSubmit,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(AppDimens.Spacing.xl3)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            SellerHeader()
            PhotosSection(
                onTakePhotoClick = onTakePhotoClick,
                onUploadClick = onUploadClick,
                files = state.uploads,
            )
            TipsSection()
            PromptSection(
                value = state.prompt,
                enabled = !state.isLoading,
                onValueChange = onPromptChanged,
            )
            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = AppTheme.colors.error,
                    fontSize = AppDimens.TextSize.xl2,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun PromptSection(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl7),
        color = AppTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl6),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            Text(
                text = stringResource(Res.string.describe_your_item),
                fontSize = AppDimens.TextSize.xl5,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onSurface,
            )
            Input(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.describe_item_placeholder),
                minLines = 3,
                maxLines = 5,
            )
        }
    }
}

@Composable
private fun SellerHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.BorderRadius.xl11))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppTheme.colors.warningVariant,
                        AppTheme.colors.warning,
                    )
                )
            )
            .padding(AppDimens.Spacing.xl6)
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(AppDimens.Size.xl21 + AppDimens.Size.xl)
                .align(Alignment.TopEnd)
                .offset(
                    x = AppDimens.Spacing.xl10,
                    y = -(AppDimens.Size.xl8 + AppDimens.Size.xl4),
                )
                .background(AppTheme.colors.onPrimary.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(AppDimens.Size.xl14 + AppDimens.Size.xl9)
                .align(Alignment.BottomStart)
                .offset(
                    x = -(AppDimens.Spacing.xl5 + AppDimens.Spacing.l),
                    y = AppDimens.Spacing.xl5 + AppDimens.Spacing.l,
                )
                .background(AppTheme.colors.onPrimary.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl5)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl)
            ) {
                IconWithBackground(
                    modifier = Modifier.size(AppDimens.Size.xl11),
                    backgroundColor = AppTheme.colors.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_snap_logo),
                        contentDescription = null,
                        tint = AppTheme.colors.warning,
                    )
                }
                Text(
                    text = stringResource(Res.string.sellsnap_title),
                    color = AppTheme.colors.onPrimary,
                    fontSize = AppDimens.TextSize.xl6,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(Res.string.turn_stuff_into_olx_listings),
                color = AppTheme.colors.onPrimary,
                fontSize = AppDimens.TextSize.xl7,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = AppDimens.TextSize.xl8
            )

            Text(
                text = stringResource(Res.string.snap_photo_ad_desc),
                color = AppTheme.colors.onPrimary.copy(alpha = 0.9f),
                fontSize = AppDimens.TextSize.xl3,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TipsSection(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl7),
        color = AppTheme.colors.infoSurface
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.Spacing.xl5),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            IconWithBackground(
                modifier = Modifier.size(AppDimens.Size.xl11),
                backgroundColor = AppTheme.colors.infoSurfaceVariant,
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = AppTheme.colors.primary
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l)
            ) {
                Text(
                    text = stringResource(Res.string.tips_for_better_photos),
                    fontSize = AppDimens.TextSize.xl4,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface
                )

                TipItem(text = stringResource(Res.string.tip_lighting))
                TipItem(text = stringResource(Res.string.tip_angles))
                TipItem(text = stringResource(Res.string.tip_defects))
            }
        }
    }
}

@Composable
private fun TipItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier.size(AppDimens.Size.xl4),
            tint = AppTheme.colors.success
        )
        Text(
            text = text,
            fontSize = AppDimens.TextSize.xl2,
            color = AppTheme.colors.onSurfaceMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun GenerateAdScreenPreview() {
    AppTheme {
        GenerateAdScreenContent(
            state = GenerateAdContract.GenerateAdState(),
            snackbarHostState = remember { SnackbarHostState() },
            onPromptChanged = {},
            onTakePhotoClick = {},
            onUploadClick = {},
            onSubmitClick = {},
        )
    }
}
